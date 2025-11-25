from datetime import datetime
from flask_sqlalchemy import SQLAlchemy
from werkzeug.security import generate_password_hash, check_password_hash

# 注意：这里的 db 是从 app.py 导入的实例
from app import db

class User(db.Model):
    __tablename__ = 'users'

    id = db.Column(db.Integer, primary_key=True) # UID, 自动递增
    username = db.Column(db.String(80), unique=True, nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=False)
    phone = db.Column(db.String(20), unique=True, nullable=False)
    password_hash = db.Column(db.String(128), nullable=False)
    registrations = db.relationship('Registration', backref='author', lazy=True, cascade="all, delete-orphan")
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    def set_password(self, password):
        self.password_hash = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password_hash, password)

    def __repr__(self):
        return f"<User('{self.username}', '{self.email}')>"

class Registration(db.Model):
    __tablename__ = 'registrations'

    id = db.Column(db.Integer, primary_key=True)
    event_name = db.Column(db.String(100), nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id', ondelete='CASCADE'), nullable=False)
    registered_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)

    def __repr__(self):
        return f"<Registration('{self.event_name}', '{self.registered_at}')>"

    # 方便地将对象转换为字典，用于 JSON 序列化
    def to_dict(self):
        return {
            'id': self.id,
            'event_name': self.event_name,
            'user_id': self.user_id,
            'username': self.author.username, # 包含报名用户的用户名
            'registered_at': self.registered_at.isoformat() # 将 datetime 转换为 ISO 格式字符串
        }