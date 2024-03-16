db.createUser(
  {
    user: "local_dev_user",
    pwd: "local_dev_password",
    roles: [ { role: "readWrite", db: "local_dev_db" },
             { role: "read", db: "reporting" } ]
  }
)
