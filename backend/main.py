from flask import Flask, jsonify, request
import jwt
from flask_cors import CORS
from openpyxl import load_workbook
from waitress import serve

app = Flask(__name__)

@app.route('/api/login', methods=['POST'])
def login():
    #do some thing in here
    print("login")

@app.route('/api/register', methods=['POST'])
def register():
    print("register")