from flask import Flask, request, jsonify
from flask_cors import CORS
from flask_sqlalchemy import SQLAlchemy
from flask_jwt_extended import JWTManager, create_access_token, jwt_required, get_jwt_identity
from werkzeug.security import generate_password_hash, check_password_hash
from datetime import datetime

# 1. 初始化 Flask 应用
app = Flask(__name__)
CORS(app, resources=r"/api/*")
# 2. 配置（替换为你的 MySQL 信息）
app.config['SECRET_KEY'] = 'mzk-app-api-key-114514'
app.config['JWT_SECRET_KEY'] = 'mzk-secret-app-api-key-114514'
app.config['JWT_ACCESS_TOKEN_EXPIRES'] = 1440
# MySQL 配置（必改：替换 root 密码和数据库名）
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://mzk:mzk114514@localhost:3306/mzk'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

app.config['SQLALCHEMY_POOL_RECYCLE'] = 300  # 5分钟强制回收连接（小于MySQL默认8小时超时）
app.config['SQLALCHEMY_POOL_PRE_PING'] = True  # 查询前自动ping，失效则重建连接
app.config['SQLALCHEMY_POOL_SIZE'] = 10
app.config['SQLALCHEMY_MAX_OVERFLOW'] = 5

# 3. 初始化扩展（必须在 app 配置后）
db = SQLAlchemy(app)  # 直接绑定 app，避免 init_app 问题
jwt = JWTManager(app)


# 4. 定义模型（必须在 db 初始化后）
class User(db.Model):
    __tablename__ = 'users'
    id = db.Column(db.Integer, primary_key=True)  # UID 自增
    username = db.Column(db.String(80), unique=True, nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=False)
    phone = db.Column(db.String(20), unique=True, nullable=False)
    password_hash = db.Column(db.String(512), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    # 关联报名记录
    registrations = db.relationship('Registration', backref='author', lazy=True, cascade="all, delete-orphan")

    def set_password(self, password):
        self.password_hash = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password_hash, password)


class Registration(db.Model):
    __tablename__ = 'registrations'
    id = db.Column(db.Integer, primary_key=True)
    event_name = db.Column(db.String(100), nullable=False)  # 活动名称
    # 新增字段
    game_username = db.Column(db.String(80), nullable=False)  # 游戏用户名（必填）
    team_name = db.Column(db.String(100), nullable=False)     # 参与车队名称（必填）
    group_type = db.Column(db.String(50), nullable=False)     # 参与组别（必填，选择框值）
    supplement = db.Column(db.Text, nullable=True)            # 补充说明（可选，纯文本）
    # 关联字段
    user_id = db.Column(db.Integer, db.ForeignKey('users.id', ondelete='CASCADE'), nullable=False)
    registered_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)

    def to_dict(self):
        return {
            'id': self.id,
            'event_name': self.event_name,
            'game_username': self.game_username,  # 新增返回字段
            'team_name': self.team_name,          # 新增返回字段
            'group_type': self.group_type,        # 新增返回字段
            'supplement': self.supplement,        # 新增返回字段
            'user_id': self.user_id,
            'username': self.author.username,
            'registered_at': self.registered_at.isoformat()
        }


# 5. JWT 用户加载器
@jwt.user_lookup_loader
def user_lookup_callback(_jwt_header, jwt_data):
    identity = jwt_data["sub"]
    return User.query.get(int(identity))


# 6. 接口路由
# 6.1 注册
@app.route('/api/register', methods=['POST'])
def register():
    data = request.get_json()
    # 校验必填字段
    required = ['username', 'email', 'phone', 'password']
    if not all(k in data for k in required):
        return jsonify({"error": "Missing required fields"}), 400
    # 校验唯一性
    if User.query.filter_by(username=data['username']).first():
        return jsonify({"error": "Username already exists"}), 409
    if User.query.filter_by(email=data['email']).first():
        return jsonify({"error": "Email already exists"}), 409
    if User.query.filter_by(phone=data['phone']).first():
        return jsonify({"error": "Phone already exists"}), 409
    # 创建用户
    user = User(
        username=data['username'],
        email=data['email'],
        phone=data['phone']
    )
    user.set_password(data['password'])
    db.session.add(user)
    db.session.commit()
    return jsonify({"message": "User created", "user_id": user.id}), 201


# 6.2 登录
@app.route('/api/login', methods=['POST'])
def login():
    if not request.is_json:
        return jsonify({"error": "Missing JSON in request"}), 400
    data = request.get_json()
    if not data.get('username') or not data.get('password'):
        return jsonify({"error": "Missing username/password"}), 400
    # 验证用户
    user = User.query.filter_by(username=data['username']).first()
    if not user or not user.check_password(data['password']):
        return jsonify({"error": "Invalid credentials"}), 401
    # 生成 Token
    access_token = create_access_token(identity=user.id)
    return jsonify({
        "access_token": access_token,
        "user": {"id": user.id, "username": user.username, "email": user.email}
    }), 200


# 6.3 获取当前用户信息
@app.route('/api/user/me', methods=['GET'])
@jwt_required()
def get_me():
    user = User.query.get(get_jwt_identity())
    return jsonify({
        "id": user.id,
        "username": user.username,
        "email": user.email,
        "phone": user.phone,
        "created_at": user.created_at.isoformat()
    }), 200


# 6.4 活动报名
@app.route('/api/events/register', methods=['POST'])
@jwt_required()
def event_register():
    if not request.is_json:
        return jsonify({"error": "Missing JSON in request"}), 400
    data = request.get_json()

    # 校验必填字段（包含新增字段）
    required_fields = ['event_name', 'game_username', 'team_name', 'group_type']
    if not all(k in data for k in required_fields):
        return jsonify({
            "error": "Missing required fields",
            "required": required_fields  # 明确提示缺少哪些字段
        }), 400

    # 创建报名记录（包含新增字段）
    reg = Registration(
        event_name=data['event_name'],
        game_username=data['game_username'],
        team_name=data['team_name'],
        group_type=data['group_type'],
        supplement=data.get('supplement', ''),  # 可选字段，无则为空字符串
        user_id=get_jwt_identity()
    )
    db.session.add(reg)
    db.session.commit()

    return jsonify({
        "message": "Registered successfully",
        "registration": reg.to_dict()
    }), 201


# 6.5 查看所有报名（整合筛选+总数统计）
@app.route('/api/events', methods=['GET'])
@jwt_required()
def get_events():
    # 1. 获取前端传入的筛选参数（可选）
    group_type = request.args.get('group_type')  # 组别筛选
    event_name = request.args.get('event_name')  # 新增：活动名称筛选（可选扩展）

    # 2. 构建基础查询
    query = Registration.query.order_by(Registration.registered_at.desc())

    # 3. 应用筛选条件（支持多条件组合）
    if group_type:
        query = query.filter(Registration.group_type == group_type)
    if event_name:
        query = query.filter(Registration.event_name.like(f"%{event_name}%"))  # 模糊匹配

    # 4. 执行查询并统计总数
    regs = query.all()
    total = len(regs)

    # 5. 返回结果（含总数+筛选后列表）
    return jsonify({
        "total": total,  # 报名记录总数（筛选后）
        "registrations": [r.to_dict() for r in regs]
    }), 200


# 6.6 查看当前用户报名
@app.route('/api/events/my', methods=['GET'])
@jwt_required()
def get_my_events():
    regs = Registration.query.filter_by(user_id=get_jwt_identity()).order_by(Registration.registered_at.desc()).all()
    return jsonify({
        "total": len(regs),  # 新增：当前用户报名总数
        "registrations": [r.to_dict() for r in regs]
    }), 200


# 8. 启动应用
if __name__ == '__main__':
    app.run(debug=False)
