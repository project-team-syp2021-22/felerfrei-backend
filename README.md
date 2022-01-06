# Felerfrei-backend

## Email

### Setup

Add a `email.config` file to the resources folder.

Example:

```lombok.config
username=youremail
password=yourpassword
host=yourhost (e.g. smtp.gmail.com)
port=yourport (e.g. 587)
```

### Usage

```java
// ...

@Autowired
private Mailsender mailsender;

public void foo() {
        // ...
        mailSender.sendVerificationEmail(saved,"http://localhost:3000/verify/");
        // ...
}
```