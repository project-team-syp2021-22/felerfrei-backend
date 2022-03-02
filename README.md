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

void foo() {
        // ...
        mailSender.sendVerificationEmail(saved,"http://localhost:3000/verify/");
        // ...
}
```

## Order-Confirmation

### Setup

Please provide a 'orderconfirmations' directory.

### Usage

```java
// ...

@Autowired
private PDFOrderConfirmationService orderConfirmationService;

void foo() {
        // ...
        Order order = ....;
        orderConfirmationService.writePDF(order);
        // ...
}
```

## Images

### Setup

Please provide a 'images' directory.