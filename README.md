# boot-livereload
[![Clojars Project](http://clojars.org/deraen/boot-livereload/latest-version.svg)](http://clojars.org/deraen/boot-livereload)

[Boot](https://github.com/boot-clj/boot) task to create a [LiveReload.js](http://livereload.com/) server.
Useful with [LiveReload Chrome plugin](https://chrome.google.com/webstore/detail/livereload/jnihajbhpnppcggbcgedagnkighmdlei).
Uses [clj-livereload](https://github.com/bhurlow/clj-livereload).

This works well with static page generators such as [Perun](https://github.com/hashobject/perun).
For use with ClojureScript apps, you should be looking at [boot-reload](https://github.com/adzerk-oss/boot-reload)
instead.

* Provides the `livereload` task

## Usage

```bash
$ boot livereload --help
```

## License

Copyright © 2015 Juho Teperi

Distributed under the MIT License.
