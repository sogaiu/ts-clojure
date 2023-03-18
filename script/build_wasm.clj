(ns build-wasm
  (:require [babashka.fs :as fs]
            [babashka.process :as proc]
            [conf :as cnf]))

(defn -main
  [& _args]
  (when-not (fs/exists? (cnf/wasm-path))
    (try
      (println "Building wasm file...")
      ;; XXX: not platform-independent
      ;; https://github.com/emscripten-core/emsdk/issues/1142 \
      ;;         #issuecomment-1334065131
      (let [path-with-emcc (str (fs/absolutize "emsdk")
                                "/upstream/emscripten" ":"
                                (System/getenv "PATH"))
            p (proc/process {:dir (cnf/grammar :dir)
                             :extra-env {"PATH" path-with-emcc}}
                            cnf/ts-bin-path "build-wasm")
            exit-code (:exit @p)]
        (when-not (zero? exit-code)
          (println "Problem building wasm file:" exit-code)
          (System/exit 1)))
      (catch Exception e
        (println "Problem building wasm file:" (.getMessage e))
        (System/exit 1)))))

