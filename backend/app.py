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
app.config['SECRET_KEY'] = 'dev-secret-key-123456'
app.config['JWT_SECRET_KEY'] = 'jwt-secret-key-654321'
# MySQL 配置（必改：替换 root 密码和数据库名）
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://mzk:mzk114514@localhost:3306/mzk'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

app.config['SQLALCHEMY_POOL_SIZE'] = 10
app.config['SQLALCHEMY_MAX_OVERFLOW'] = 5
app.config['SQLALCHEMY_POOL_RECYCLE'] = 300
app.config['SQLALCHEMY_POOL_PRE_PING'] = True

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
    event_name = db.Column(db.String(100), nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id', ondelete='CASCADE'), nullable=False)
    registered_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)

    def to_dict(self):
        return {
            'id': self.id,
            'event_name': self.event_name,
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
    if not request.is_json:
        return jsonify({"error": "Missing JSON in request"}), 400
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
    if not data.get('event_name'):
        return jsonify({"error": "Event name required"}), 400
    # 创建报名记录
    reg = Registration(
        event_name=data['event_name'],
        user_id=get_jwt_identity()
    )
    db.session.add(reg)
    db.session.commit()
    return jsonify({"message": "Registered", "registration": reg.to_dict()}), 201

# 6.5 查看所有报名
@app.route('/api/events', methods=['GET'])
@jwt_required()
def get_events():
    regs = Registration.query.order_by(Registration.registered_at.desc()).all()
    return jsonify({"registrations": [r.to_dict() for r in regs]}), 200

# 7. 初始化数据库（关键：创建表）
with app.app_context():
    try:
        db.create_all()  # 强制创建所有表
        print("✅ 数据库表创建成功！")
    except Exception as e:
        print(f"❌ 表创建失败：{str(e)}")

# 8. 启动应用
if __name__ == '__main__':
    app.run(debug=True)