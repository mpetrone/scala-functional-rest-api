This is a example proyect for a fuctional api rest using:

* http4s for the http endponts
* doobie for Db
* circe for json

Motivation
----------
There are a few examples using the same libraries but neither of them
solves the problems you may encounter in real life.
This is how i resolved the struggles in building  a simple but extensible functional http endpoint with
CRUD implementation.

Run
---

Using [sbt-revolver](https://github.com/spray/sbt-revolver) plugin we can have hot reloading for faster development

Just run
```
sbt hot-run
```

Test
----

To run the tests just run
```
sbt test
```

Contributing
-----

Your contributions are always welcome! Please submit a pull request or
create an issue if you see something wrong