# Prepare Source Samples

## General Info

A set of samples is defined in `conf/conf.clj` like:

```clojure
(def clojars
  {:name "clojars"
   :root (str proj-root "/clojars-samples/data/clojars-repos")
   :extensions #{"bb" "nbb"
                 "clj" "cljc" "cljd" "cljr" "cljs" "cljx"
                 "dtm" "edn"}
   :error-file-paths (str proj-root "/data/clojars-error-files.txt")})
```

That is, `clojars` refers to a map with information about:

* where on the local filesystem to look for files
* which files to parse based on file extension
* where to store the names of files that yielded errors

You can define your own map along with an appropriate directory and
set of files / directories, and then point `repos` at it.

## Clojars

### On the subject of speed...

Retrieving jars from Clojars is a slow process.  At the time of this
writing, it took about 11 hours to retrieve somewhat over 20,000 jars.
It's likely this depends a bit on where one is located though.

The retrieval process was designed to avoid taxing Clojars [1].
Trying to make it go faster by tweaking values in the scripts may not
work so well as you may get throttled or temporarily blocked.  It
might be possible to get it to go somewhat faster without issue but
this has not really been attempted.

Be nice...and patient (^^;

### Which Jars Exactly?

The jars are retrieved using `curl`, indirectly based on a list in
`data/clojars-jar-list.txt`.  This list already lives in this
repository, but a new one (which may end up with different content)
can be generated via a Babashka task.

The Babashka task fetches `data/feed.clj` from Clojars and creates a
list of URLs of "latest release" jars and saves this in the
aforementioned `data/clojars-jar-list.txt`.

Using a new list is not recommended unless you're willing to comb
through new error output.  Still, it might be worth it at some point
because when the current list was generated, files with certain
extensions were quite under-represented.

To make a new list, first remove:

* `data/feed.clj`
* `data/clojars-jar-list.txt`

then execute:

```
bb make-jars-list
```

### Example Invocations

To fetch 1000 jars, execute:

```
bb fetch-jars 1000
```

To fetch the maximum number of jars that makes sense to [2], try:

```
bb fetch-jars -1
```

Note that after fetching jars, the content needs to be extracted.
This can be done manually, but it's likely more convenient to do:

```
bb extract-jars
```

### Misc Info

It turns out that there can be duplicate files across the content of
extracted jars.  You may be able to speed up the testing process (and
reduce local storage usage) by deduplicating.  There used to be code
to do this, but it has been removed for maintenance reasons.  Likely
some existing deduplication program will work fine.

---

[1] It's possible it's still too taxing.  If you know this to be the
case, please mention it so appropriate measures might be considered.

[2] For a certain defintion of "makes sense to" :)
