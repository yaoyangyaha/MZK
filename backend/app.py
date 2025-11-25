from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from flask_jwt_extended import JWTManager
from config import Config

# init
db = SQLAlchemy()
jwt = JWTManager()


def create_app(config_class=Config):
    app = Flask(__name__)
    app.config.from_object(config_class)

    # init
    db.init_app(app)
    jwt.init_app(app)

    # reg api
    from routes import api as api_bp
    app.register_blueprint(api_bp, url_prefix='/api') # 所有 API 路由都以 /api 开头

    # create table
    with app.app_context():
        db.create_all()
        print("数据库表已创建或已存在。")

    return app


if __name__ == '__main__':
    # let us go!
    app = create_app()
    app.run(debug=True)