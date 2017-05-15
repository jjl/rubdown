The irresponsible clojure guild presents...

# rubdown

Flexible, performant markdown for clojure

## Umm what? Doesn't this exist?

You'd be surprised. I found two libraries based on pegdown
(deprecated) and one based on commonmark. I needed a parser that
supported github flavoured markdown for publishing the irresponsible
site, so I had to write it...

Flexdown (the java library wrapped by rubdown) has a broad range of support,
so there's a good chance it can parse whatever you have to work with.

## Usage

Here's how you could render this README.md in the repl:

```clojure
(require '[irresponsible.rubdown :as r])
(def dialect (r/dialect :github)) ;; this README is github-flavoured markdown
(def p (r/parser dialect)) ;; construct a parser for GHFM
(def h (r/html-renderer dialect)) ;; and a html renderer for GHFM
(spit "README.html" (r/render-html h (r/parse-file p "README.md"))) ;; magic!
```

## Notes

* Does not support clojurescript and probably never will for technical reasons.
* Currently only supports GHFM and CommonMark dialects. Others coming soon!
  
## Copyright and License

Copyright (c) 2017 James Laver

MIT LICENSE

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

