(ns clojure-bot.core
  (:require [clojure-bot.api :as api])
  (:gen-class))

(def game-info (atom {}))
(def run? (atom true))

(defn start
  []
  (reset! run? ))

(defn run
  []
  (let [x (api/register "🫥")]
    (reset! game-info x)

    (while @run?
      (Thread/sleep 1000)
      (println "runnin: " (pr-str @run?))
      ;; You probably want to get the current game-state from the server before you do your move
      (api/move (:id @game-info) (rand-nth ["LEFT" "RIGHT"])))))



(defn -main
  [& args]
  ;; game-info contains the game map
  (run))