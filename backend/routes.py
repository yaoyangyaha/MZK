from flask import Blueprint, request, jsonify
from flask_jwt_extended import create_access_token, create_refresh_token, jwt_required
from flask_jwt_extended import get_jwt_identity, current_user
from werkzeug.security import generate_password_hash

from app import db, jwt
from models import User, Registration

api = Blueprint('api', __name__)


# --- JWT 辅助函数 ---
# 这个函数用于从数据库加载用户，以便在受保护的路由中使用 current_user
@jwt.user_lookup_loader
def user_lookup_callback(_jwt_header, jwt_data):
    identity = jwt_data["sub"]
    return User.query.get(identity)


# --- API 错误处理 ---
@api.errorhandler(404)
def not_found(e):
    return jsonify({"error": "Resource not found"}), 404


@api.errorhandler(400)
def bad_request(e):
    return jsonify({"error": "Bad request"}), 400


# --- 认证相关 Endpoints ---

@api.route('/register', methods=['POST'])
def register():
    """
    用户注册
    ---
    请求体:
      {
        "username": "johndoe",
        "email": "john@example.com",
        "phone": "1234567890",
        "password": "securepassword"
      }
    响应:
      {
        "message": "User created successfully",
        "user_id": 1
      }
    """
    if not request.is_json:
        return jsonify({"error": "Missing JSON in request"}), 400

    data = request.get_json()

    required_fields = ['username', 'email', 'phone', 'password']
    if not all(field in data for field in required_fields):
        return jsonify({"error": "Missing required fields"}), 400

    username = data.get('username')
    email = data.get('email')
    phone = data.get('phone')
    password = data.get('password')

    if User.query.filter_by(username=username).first():
        return jsonify({"error": "Username already exists"}), 409

    if User.query.filter_by(email=email).first():
        return jsonify({"error": "Email already exists"}), 409

    if User.query.filter_by(phone=phone).first():
        return jsonify({"error": "Phone number already exists"}), 409

    new_user = User(username=username, email=email, phone=phone)
    new_user.set_password(password)

    db.session.add(new_user)
    db.session.commit()

    return jsonify({
        "message": "User created successfully",
        "user_id": new_user.id
    }), 201


@api.route('/login', methods=['POST'])
def login():
    """
    用户登录，获取 Token
    ---
    请求体:
      {
        "username": "johndoe",
        "password": "securepassword"
      }
    响应:
      {
        "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "user": {
          "id": 1,
          "username": "johndoe",
          "email": "john@example.com"
        }
      }
    """
    if not request.is_json:
        return jsonify({"error": "Missing JSON in request"}), 400

    data = request.get_json()
    username = data.get('username')
    password = data.get('password')

    if not username or not password:
        return jsonify({"error": "Missing username or password"}), 400

    user = User.query.filter_by(username=username).first()

    if not user or not user.check_password(password):
        return jsonify({"error": "Invalid credentials"}), 401

    # 创建访问令牌和刷新令牌
    access_token = create_access_token(identity=user.id, fresh=True)
    refresh_token = create_refresh_token(identity=user.id)

    return jsonify({
        "access_token": access_token,
        "refresh_token": refresh_token,
        "user": {
            "id": user.id,
            "username": user.username,
            "email": user.email
        }
    }), 200


# --- 受保护的 Endpoints ---

@api.route('/user/me', methods=['GET'])
@jwt_required()
def get_user_profile():
    """
    获取当前登录用户的信息
    ---
    请求头:
      Authorization: Bearer <access_token>
    响应:
      {
        "id": 1,
        "username": "johndoe",
        "email": "john@example.com",
        "phone": "1234567890",
        "created_at": "2023-10-27T12:00:00"
      }
    """
    # current_user 由 @jwt.user_lookup_loader 加载
    return jsonify({
        "id": current_user.id,
        "username": current_user.username,
        "email": current_user.email,
        "phone": current_user.phone,
        "created_at": current_user.created_at.isoformat()
    }), 200


@api.route('/events/register', methods=['POST'])
@jwt_required()
def register_for_event():
    """
    为当前用户报名活动
    ---
    请求头:
      Authorization: Bearer <access_token>
    请求体:
      {
        "event_name": "Annual Tech Conference"
      }
    响应:
      {
        "message": "Successfully registered for the event",
        "registration": {
          "id": 1,
          "event_name": "Annual Tech Conference",
          "user_id": 1,
          "username": "johndoe",
          "registered_at": "2023-10-27T12:30:00"
        }
      }
    """
    if not request.is_json:
        return jsonify({"error": "Missing JSON in request"}), 400

    data = request.get_json()
    event_name = data.get('event_name')

    if not event_name:
        return jsonify({"error": "Event name is required"}), 400

    new_registration = Registration(event_name=event_name, user_id=current_user.id)

    db.session.add(new_registration)
    db.session.commit()

    return jsonify({
        "message": "Successfully registered for the event",
        "registration": new_registration.to_dict()
    }), 201


@api.route('/events', methods=['GET'])
@jwt_required()
def get_all_events():
    """
    获取所有活动报名记录（按报名时间降序排序）
    ---
    请求头:
      Authorization: Bearer <access_token>
    响应:
      {
        "registrations": [
          {
            "id": 2,
            "event_name": "Workshop",
            "user_id": 2,
            "username": "janedoe",
            "registered_at": "2023-10-27T14:00:00"
          },
          {
            "id": 1,
            "event_name": "Annual Tech Conference",
            "user_id": 1,
            "username": "johndoe",
            "registered_at": "2023-10-27T12:30:00"
          }
        ]
      }
    """
    registrations = Registration.query.order_by(Registration.registered_at.desc()).all()
    return jsonify({
        "registrations": [reg.to_dict() for reg in registrations]
    }), 200


@api.route('/events/my', methods=['GET'])
@jwt_required()
def get_my_events():
    """
    获取当前用户的所有活动报名记录
    ---
    请求头:
      Authorization: Bearer <access_token>
    响应:
      {
        "registrations": [
          {
            "id": 1,
            "event_name": "Annual Tech Conference",
            "user_id": 1,
            "username": "johndoe",
            "registered_at": "2023-10-27T12:30:00"
          }
        ]
      }
    """
    registrations = Registration.query.filter_by(user_id=current_user.id).order_by(
        Registration.registered_at.desc()).all()
    return jsonify({
        "registrations": [reg.to_dict() for reg in registrations]
    }), 200