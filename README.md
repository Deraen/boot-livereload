# boot-livereload
[![Clojars Project](http://clojars.org/deraen/boot-livereload/latest-version.svg)](http://clojars.org/deraen/boot-livereload)

[Boot](https://github.com/boot-clj/boot) task to create a [LiveReload.js](http://livereload.com/) server. Automatically injects client JS to HTML files.

This works well with static page generators such as [Perun](https://github.com/hashobject/perun).
For use with ClojureScript apps, you should be looking at [boot-reload](https://github.com/adzerk-oss/boot-reload)
instead.

* Provides the `livereload` task

## Usage

```bash
$ boot livereload --help
```

### Dynamically served HTML file

If your HTML file is not in the fileset, the task can't automatically inject the JS code to page. In this case you should use the default port (35729) for Livereload server and either 1) use [browser extensions](http://livereload.com/extensions/), 2) [add JS manually](http://feedback.livereload.com/knowledgebase/articles/86180-how-do-i-add-the-script-tag-manually).

## License

Copyright © 2015-2017 Juho Teperi

Distributed under the MIT License.
