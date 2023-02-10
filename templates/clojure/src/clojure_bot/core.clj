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

(defn my-user
  [game-state name]
  (-> game-state
      :players
      (->> (filter #(= (:name %) name)))
      first))

(defn my-position
  [game-state name]
  (:position (my-user game-state name)))

(defn closest-item
  [items position]
  (-> items (->> (sort-by #(distance position (:position %)))) first))

(def user-name "🫥")

(defn what-move
  [game-state name]
  (let [_ (reset! game-state-atom game-state)
        my-health (:health (my-user game-state name))
        my-position (my-position game-state name)
        closest (closest-item (:items game-state) my-position)]
    #_(println "my-pos" (pr-str my-position) "closest: " (pr-str (:position closest)))

    (println "my health: " my-health)
    (let [xdif (- (-> closest :position :x) (:x my-position))
          ydif (- (-> closest :position :y) (:y my-position))
          xdist (Math/abs ^int xdif)
          ydist (Math/abs ^int ydif)
          dirr (cond (= [0 0] [xdist ydist])
                     "PICK"
                     (and (<= xdist ydist) (< 0 ydif))
                     "DOWN"
                     (<= xdist ydist)
                     "UP"
                     (and (< ydist xdist) (< 0 xdif))
                     "RIGHT"
                     (< ydist xdist)
                     "LEFT")]
      dirr)))

(defn run
  []
  (let [x (api/register user-name)]
    (reset! game-info x)

    (while true
      (println "cycle")
      (Thread/sleep 500)
      (let [game-state (api/game-state)]
        (api/move (:id @game-info) (what-move game-state user-name)))

      ;; You probably want to get the current game-state from the server before you do your move
      )))

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
