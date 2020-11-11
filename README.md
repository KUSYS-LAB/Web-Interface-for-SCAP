# Web-Interface-for-SCAP
The Web Interface for SCAP project.

The project is under development with the support of KHIDI(https://www.khidi.or.kr/kps).
All description you need will be added soon.

## Set up
Before setting up this project, you should set the followings up first:
```
[ca](https://github.com/KUSYS-LAB/Certificate-Authority-for-SCAP/)
[as](https://github.com/KUSYS-LAB/Authentication-Server-for-SCAP/)
[cs](https://github.com/KUSYS-LAB/Code-Signer-for-SCAP/)
```
Currently we use the mariadb for dbms, so you have to install the mariadb first.
If you have installed the mariadb, you have to create the db and the account.
By default, we use the following db and account. If you want to other account and db, you have to modify `application.properties`.
```
DB: cdm_web
ID: cdm_web_admin
PW: cdm_web_admin
```

This project has the interaction with ca, as, and cs. So the address of these should be set up at `ca.domain`, `as.domain`, and `cs.domain` in `application.properties`.
Now, all set up.
