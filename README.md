# Data Circuit Database Application

A web app developed for assisting Mobitel in circuit management.

## Features

* User management system
* Dashboard

## Prerequisites

* You will need [Leiningen][1] 2.0 or above installed.
* H2 Database

[1]: https://github.com/technomancy/leiningen

## Running

Add database url to enviroment
```
export DATABASE_URL="jdbc:h2:./app.db"
```

Create an admin account:
```
lein createadmin [options]
    -f, --first_name  First Name
    -l, --last_name   Last Name
    -e, --email       Email Address
    -p, --password    Password
```

Run migrations
```
lein run migrate
```

To start a web server for the application, run:
```
lein run [options]
    -p, --port    Port
```

## License

Copyright © 2017 FIXME
