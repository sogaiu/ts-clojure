(ns conf
  (:require [babashka.fs :as fs]
            [clojure.string :as cs]))

(def verbose
  (System/getenv "VERBOSE"))

(def proj-root (fs/cwd))

;; tree-sitter

(def ts-ref
  "v0.20.7")

(def ts-repo-url
  "https://github.com/tree-sitter/tree-sitter")

(def abi 13)

(def ts-bin-path
  (str proj-root "/bin/tree-sitter"))

(def ts-generate-cmd
  (str ts-bin-path " generate --abi " abi " --no-bindings"))

;; relative to proj-root
(def ts-cli-real-path
  "tree-sitter/target/release/tree-sitter")

;; relative to proj-root
(def ts-cli-link-path
  "bin/tree-sitter")

;; tree-sitter-clojure

(def tsclj-ref
  "pre-0.0.12")

(def tsclj-dir
  "tree-sitter-clojure")

(def tsclj-repo-url
  "https://github.com/sogaiu/tree-sitter-clojure")

(def grammar-js
  (str proj-root "/" tsclj-dir "/grammar.js"))

(def tsclj-src-dir
  (str proj-root "/" tsclj-dir "/src"))

(def parser-c-name
  "parser.c")

(def parser-c
  (str tsclj-src-dir "/" parser-c-name))

(def grammar-json
  (str tsclj-src-dir "/grammar.json"))

(def node-types-json
  (str tsclj-src-dir "/node-types.json"))

(def tree-sitter-headers-dir
  (str tsclj-src-dir "/tree_sitter"))

(def generated-source-paths
  [parser-c grammar-json node-types-json tree-sitter-headers-dir])

(def dynamic-library-extension
  (let [os (cs/lower-case (System/getProperty "os.name"))]
    (cond
      (cs/starts-with? os "win")
      "dll"
      ;;
      (cs/starts-with? os "mac")
      "dylib"
      ;; XXX: probably works most of the time?
      :else
      "so")))

(def tsclj-lib-name
  (str "clojure." dynamic-library-extension))

(def tsclj-dynamic-library-path
  (str (fs/xdg-cache-home)
       "/tree-sitter/lib"
       "/"
       tsclj-lib-name))

(def c-compiler
  "cc")

;; clojars

(def clojars-extensions
  #{"clj" "cljc" "cljd" "cljr" "cljs" "cljx" "dtm" "edn" "bb" "nbb"})

(def clojars-file-exts-path
  (str proj-root "/data/clojars-file-exts.txt"))

(def feed-clj-path
  (str proj-root "/data/feed.clj"))

(def feed-clj-gz-path
  (str proj-root "/data/feed.clj.gz"))

(def clru-list-path
  (str proj-root "/data/latest-release-jar-urls.txt"))

(def clojars-repos-root
  (str proj-root "/data/clojars-repos"))

(def clojars-jars-root
  (str proj-root "/data/clojars-jars"))

(def clojars-jar-file-paths
  (str proj-root "/data/clojars-jar-files.txt"))

(def clojars-file-paths
  (str proj-root "/data/clojars-files.txt"))

(def clojars-skip-urls
  (str proj-root "/data/clojars-skip-urls.txt"))

(def clojars-error-file-paths
  (str proj-root "/data/clojars-error-files.txt"))

;; dewey

(def dewey-all-repos-edn-url
  (str "https://github.com/phronmophobic/dewey/releases/download/"
       "2023-03-06"
       "/all-repos.edn.gz"))

(def dewey-all-repos-edn
  (str proj-root "/data/all-repos.edn"))

(def dewey-git-repos-urls
  (str proj-root "/data/dewey-git-repos-urls.txt"))

;; clojuredart

(def cljd-repos-root
  (str proj-root "/data/clojuredart-repos"))

(def cljd-repos-list
  (str proj-root "/data/clojuredart-repos-list.txt"))

(def cljd-extensions
  #{"cljd"})

;; failures

(def fails-root
  (str proj-root "/test/expected-failures"))

(def misparses-root
  (str proj-root "/test/expected-misparses"))

