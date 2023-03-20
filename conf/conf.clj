(ns conf
  (:require [babashka.fs :as fs]
            [clojure.string :as cs]))

(def verbose
  (System/getenv "VERBOSE"))

(def proj-root (fs/cwd))

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

;; emsdk

(def emsdk-repo-url
  "https://github.com/emscripten-core/emsdk")

(def emsdk-version
  "3.1.29")

;; tree-sitter

(def ts-ref
  "1b1c3974f789a9bfaa31f493e6eaa212f13bdfb9")

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

(def parser-c-name
  "parser.c")

(def c-compiler
  "cc")

;; helper bits

(defn repo-path-to
  [root path]
  (str proj-root "/" root "/" path))

(defn src-path-to
  [root path]
  (str proj-root "/" root "/src/" path))

(defn make-inner-name
  [name]
  (cs/replace name "-" "_"))

(defn make-lib-name
  [name]
  (let [inner-name (make-inner-name name)]
    (str inner-name "." dynamic-library-extension)))

(defn make-lib-path
  [name]
  (let [lib-name (make-lib-name name)]
    (str (fs/xdg-cache-home)
         "/tree-sitter/lib"
         "/" lib-name)))

(defn gen-source-paths
  [grammar]
  (reduce (fn [acc i]
            (conj acc (i grammar)))
          []
          [:parser-c :grammar-json :node-types-json :ts-headers-dir]))

;; tree-sitter-clojure

(def ts-clj
  (let [name "clojure"
        repo-name (str "tree-sitter-" name)
        dir-name repo-name]
    {:name name
     :inner-name (make-inner-name name)
     ;;
     :repo-url (str "https://github.com/sogaiu/" repo-name)
     :ref "pre-0.0.12"
     ;;
     :dir dir-name
     :grammar-js (repo-path-to dir-name "grammar.js")
     ;;
     :src-dir (str proj-root "/" dir-name "/src")
     :parser-c (src-path-to dir-name parser-c-name)
     :grammar-json (src-path-to dir-name "grammar.json")
     :node-types-json (src-path-to dir-name "node-types-json")
     :ts-headers-dir (src-path-to dir-name "tree_sitter")
     ;;
     :lib-name (make-lib-name name)
     :lib-path (make-lib-path name)}))

;; tree-sitter-clojure-def

(def ts-clj-def
  (let [name "clojure-def"
        repo-name (str "tree-sitter-" name)
        dir-name repo-name]
    {:name name
     :inner-name (make-inner-name name)
     ;;
     :repo-url (str "https://github.com/sogaiu/" repo-name)
     :ref "default"
     ;;
     :dir dir-name
     :grammar-js (repo-path-to dir-name "grammar.js")
     ;;
     :src-dir (str proj-root "/" dir-name "/src")
     :parser-c (src-path-to dir-name parser-c-name)
     :grammar-json (src-path-to dir-name "grammar.json")
     :node-types-json (src-path-to dir-name "node-types-json")
     :ts-headers-dir (src-path-to dir-name "tree_sitter")
     ;;
     :lib-name (make-lib-name name)
     :lib-path (make-lib-path name)}))

;; current grammar setting

(def ^:dynamic grammar
  ts-clj)

(defn wasm-path
  []
  (str (grammar :dir) "/"
       "tree-sitter-" (grammar :inner-name) ".wasm"))

;; clojars

(def clojars
  {:name "clojars"
   :root (str proj-root "/data/clojars-repos")
   :extensions #{"clj" "cljc" "cljd" "cljr" "cljs" "cljx"
                 "dtm" "edn" "bb" "nbb"}
   :file-exts-path (str proj-root "/data/clojars-file-exts.txt")})

(def clojars-extensions
  #{"clj" "cljc" "cljd" "cljr" "cljs" "cljx" "dtm" "edn" "bb" "nbb"})

(def feed-clj-path
  (str proj-root "/data/feed.clj"))

(def feed-clj-gz-path
  (str proj-root "/data/feed.clj.gz"))

(def clojars-jar-list-path
  (str proj-root "/data/clojars-jar-list.txt"))

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

;; github

(def github
  {:name "github"
   :root (str proj-root "/data/github-repos")
   :extensions #{;;"clj" "cljc"
                 "cljd" "cljr"
                 ;;"cljs"
                 "cljx"
                 "dtm" "edn" "bb" "nbb"}
   :file-exts-path (str proj-root "/data/github-file-exts.txt")})




;; clojuredart

(def clojuredart
  {:name "clojuredart"
   :root (str proj-root "/data/clojuredart-repos")
   :extensions #{"clj" "cljc" "cljd"
                 "edn"}
   :file-exts-path (str proj-root "/data/clojuredart-file-exts.txt")})

(def cljd-repos-root
  (str proj-root "/data/clojuredart-repos"))

(def cljd-repos-list
  (str proj-root "/data/clojuredart-repos-list.txt"))

(def cljd-extensions
  #{"cljd"})

;; current repos setting

(def ^:dynamic repos
  clojars)

;; failures

(def fails-root
  (str proj-root "/test/expected-failures"))

(def misparses-root
  (str proj-root "/test/expected-misparses"))

