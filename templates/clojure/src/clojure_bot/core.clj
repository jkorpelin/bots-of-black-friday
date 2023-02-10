(ns clojure-bot.core
  (:require [clojure-bot.api :as api])
  (:gen-class))

(def game-info (atom {}))
(def app (atom nil))
(def game-state-atom (atom {}))

(defn get-game-state
  []
  (let [game-state (api/game-state)]
    (reset! game-state-atom game-state)))

(defn distance [p0 p1]
  (+ (Math/abs ^int (- (:x p0) (:x p1)))
     (Math/abs ^int (- (:y p0) (:y p1)))))

(defn closest-item
  [game-state name]
  (let [position
        (-> game-state
            :players
            (->> (filter #(= (:name %) name)))
            first
            :position)
        ]
    (-> game-state :items (->> (sort-by #(distance position (:position %)))) first)))

(-> @game-state-atom :players first :name)


(Math/abs -1)


()

(defn run
  []
  (let [x (api/register "🫥")]
    (reset! game-info x)

    (while true
      (println "cycle")
      (Thread/sleep 1000)
      #_(get-game-state)

      ;; You probably want to get the current game-state from the server before you do your move
      (api/move (:id @game-info) (rand-nth ["LEFT" "RIGHT"])))))

(defn start
  []
  (reset! app (future (run))))

(defn stop
  []
  (future-cancel @app))

(defn -main
  [& args]
  ;; game-info contains the game map
  (run))