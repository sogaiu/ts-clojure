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

;; tree-sitter

(def ts-ref
  "v0.20.8")

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

(defn repo-path-to
  [root path]
  (str proj-root "/" root "/" path))

(defn src-path-to
  [root path]
  (str proj-root "/" root "/src/" path))

(defn make-inner-name
  [name]
  (cs/replace name "-" "_"))

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
        dir-name repo-name
        inner-name (make-inner-name name)
        lib-name (str inner-name "." dynamic-library-extension)]
    {:name name
     :inner-name inner-name
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
     :lib-name lib-name
     :lib-path (str (fs/xdg-cache-home)
                    "/tree-sitter/lib"
                    "/" lib-name)}))

;; tree-sitter-clojure-def

(def ts-clj-def
  (let [name "clojure-def"
        repo-name (str "tree-sitter-" name)
        dir-name repo-name
        inner-name (make-inner-name name)
        lib-name (str inner-name "." dynamic-library-extension)]
    {:name name
     :inner-name inner-name
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
     :lib-name lib-name
     :lib-path (str (fs/xdg-cache-home)
                    "/tree-sitter/lib"
                    "/" lib-name)}))

;; choose which grammar

(def ^:dynamic grammar ts-clj)

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

