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
  "3.1.35")

;; tree-sitter

(def ts-ref
  "0c49d6745b3fc4822ab02e0018770cd6383a779c")

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

(def ts-conf-dir
  (let [dot-ts-dir (str proj-root "/.tree-sitter")]
    (if (fs/exists? dot-ts-dir)
      dot-ts-dir
      ;; XXX: windows?
      (str (fs/xdg-config-home)
           "/tree-sitter"))))

(def ts-lib-dir
  (let [dot-ts-dir (str proj-root "/.tree-sitter")]
    (if (fs/exists? dot-ts-dir)
      (str dot-ts-dir "/lib")
      ;; XXX: windows?
      (str (fs/xdg-cache-home)
           "/tree-sitter/lib"))))

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
    (if ts-lib-dir
      (str ts-lib-dir "/" lib-name)
      (str (fs/xdg-cache-home)
           "/tree-sitter/lib"
           "/" lib-name))))

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
     ;;:repo-url (str "https://github.com/oakmac/" repo-name)
     ;;:ref "master"
     ;;:repo-url (str "https://github.com/Tavistock/" repo-name)
     ;;:ref "master"
     ;;:repo-url (str "https://github.com/Lancear/" repo-name)
     ;;:ref "main"
     ;;:repo-url (str "https://github.com/artarf/" repo-name)
     ;;:ref "master"
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
   :root (str proj-root "/clojars-samples/data/clojars-repos")
   :extensions #{"bb" "nbb"
                 "clj" "cljc" "cljd" "cljr" "cljs" "cljx"
                 "dtm" "edn"}
   :error-file-paths (str proj-root "/data/clojars-error-files.txt")
   :file-exts-path (str proj-root "/data/clojars-file-exts.txt")})

;; github

(def github
  {:name "github"
   :root (str proj-root "/github-samples/data/github-repos")
   :extensions #{;;"clj" "cljc"
                 "cljd" "cljr"
                 ;;"cljs"
                 "cljx"
                 "dtm" "edn" "bb" "nbb"}
   :error-file-paths (str proj-root "/data/github-error-files.txt")
   :file-exts-path (str proj-root "/data/github-file-exts.txt")})

;; clojuredart

(def clojuredart
  {:name "clojuredart"
   :root (str proj-root "/clojuredart-samples/data/clojuredart-repos")
   :extensions #{"clj" "cljc" "cljd"
                 "edn"}
   :error-file-paths (str proj-root "/data/clojuredart-error-files.txt")
   :file-exts-path (str proj-root "/data/clojuredart-file-exts.txt")})

;; current repos setting

(def ^:dynamic repos
  #_ clojuredart
  #_github
  clojars)

;; failures

(def fails-root
  (str proj-root "/test/expected-failures"))

(def misparses-root
  (str proj-root "/test/expected-misparses"))

