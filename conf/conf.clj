(ns conf
  (:require [babashka.fs :as fs]))

(def verbose
  (System/getenv "VERBOSE"))

(def proj-root (fs/cwd))

;; tree-sitter-clojure

(def grammar-dir "tree-sitter-clojure")

;; tree-sitter

(def ts-bin-path "tree-sitter")

;; ABI   CLI ver  Date
;; ---   -------  ----
;; 14    0.20.8   2023-04
;; 14    0.20.9   2024-01
;; 14    0.21.0   2024-02
;; 14    0.22.0   2024-03
;; 14    0.22.1   2024-03
;; 14    0.22.2   2024-03
;; 14    0.22.3   2024-04
;; 14    0.22.4   2024-04
;; 14    0.22.5   2024-04
;; 14    0.22.6   2024-05

(def abi 14)

;; clojars

(def clojars
  {:name "clojars"
   :root (str proj-root "/clojars-samples/data/clojars-repos")
   :extensions #{"bb" "nbb"
                 "clj" "cljc" "cljd" "cljr" "cljs" "cljx"
                 "dtm" "edn"}
   :error-file-paths (str proj-root "/data/clojars-error-files.txt")})

;; clojuredart

(def clojuredart
  {:name "clojuredart"
   :root (str proj-root "/clojuredart-samples/data/clojuredart-repos")
   :extensions #{"clj" "cljc" "cljd"
                 "edn"}
   :error-file-paths (str proj-root "/data/clojuredart-error-files.txt")})

;; current repos setting

(def ^:dynamic repos
  #_ clojuredart
  clojars)

