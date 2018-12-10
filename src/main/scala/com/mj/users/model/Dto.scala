package com.mj.users.model

case class LoginDto(login: String, password: String)

case class UidDto(uid: String)

case class RegisterDto(login: String,
                       nickname: String,
                       password: String,
                       repassword: String,
                       gender: Int)

