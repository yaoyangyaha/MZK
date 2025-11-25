import os


class Config:
    SECRET_KEY = os.environ.get('SECRET_KEY') or 'a-very-secretive-key-change-this-in-production'
    # JWT 密钥，用于签名 Token
    JWT_SECRET_KEY = os.environ.get('JWT_SECRET_KEY') or 'another-very-secretive-key-for-jwt'

    SQLALCHEMY_DATABASE_URI = 'mysql+pymysql://mzk:mzk114514@localhost/mzk'
    SQLALCHEMY_TRACK_MODIFICATIONS = False

    # 可选：配置 Token 的过期时间（例如，30 分钟）
    JWT_ACCESS_TOKEN_EXPIRES = 1440  # 分钟