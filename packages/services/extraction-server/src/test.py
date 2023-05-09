from flask import Flask, request, jsonify
import jwt


def login():
    username = 'adam@test.io'
    password = 'asdf1ASDF'

    if username and password:
        config = get_jwt_auth_config()

        try:
            # Generate and return a JWT token
            payload = {'username': username}
            token = jwt.encode(payload, config["Client Secret"], algorithm='HS256')
            return token
            # return jsonify({'token': token}), 200
        except:
            return 'Failed to generate token.'
            return jsonify({'message': 'Failed to generate token.'}), 500

    return 'bad bad bad'


def protected():
    token = request.headers.get("Authorization")
    if token:
        token = token.split()[1]  # Remove 'Bearer ' from the token

        config = get_jwt_auth_config()

        try:
            # Validate the JWT token using the provided client secret
            payload = jwt.decode(token, config["Client Secret"], algorithms=["HS256"])
            return jsonify({"message": "Valid token."}), 200
        except jwt.InvalidTokenError:
            return jsonify({"message": "Invalid token."}), 401

    return jsonify({"message": "Token missing."}), 401


def get_jwt_auth_config():
    config = {
        "Token Name": None,
        "Auth URL": None,
        "Access Token URL": None,
        "Client ID": None,
        "Client Secret": None,
        "Scope": None,
    }

    global_config = {
        "values": [
            {
                "key": "Token Name",
                "value": "keycloak",
                "type": "default",
                "enabled": True,
            },
            {
                "key": "Auth URL",
                "value": "http://localhost:8079/realms/Terarium/protocol/openid-connect/auth",
                "type": "default",
                "enabled": True,
            },
            {
                "key": "Access Token URL",
                "value": "http://localhost:8079/realms/Terarium/protocol/openid-connect/token",
                "type": "default",
                "enabled": True,
            },
            {"key": "Client ID", "value": "app", "type": "secret", "enabled": True},
            {
                "key": "Client Secret",
                "value": "jtbQhs6SlfynqJaygVpwav2kLzAme2b4",
                "type": "secret",
                "enabled": True,
            },
            {"key": "Scope", "value": "openid email profile", "enabled": True},
        ]
    }

    for item in global_config["values"]:
        key = item["key"]
        value = item.get("value")
        config[key] = value

    return config


resp = login()
print(resp)
