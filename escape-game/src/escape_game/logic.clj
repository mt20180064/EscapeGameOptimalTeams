(ns logic
  (:require [clojure.string]))
(defn my-into
  [target additions]
  (apply conj additions target))

(def igraci [{:name "Nina" :experience 4 :teamplayer 1 :adroit 5 :mood 5 :theme 2 :frightened 2 :competitiveness 2 }
             {:name "Anis" :experience  4 :teamplayer 1 :adroit 4 :mood 2 :theme 1 :frightened  1 :competitiveness 1 }
             {:name "Điri" :experience  5 :teamplayer 2 :adroit 1 :mood 5 :theme  2 :frightened 2 :competitiveness 1 }
             {:name "Joča" :experience  2 :teamplayer 1 :adroit 3 :mood 1 :theme  1 :frightened  1 :competitiveness 2 }
             {:name "Maša" :experience  5 :teamplayer 2 :adroit 4 :mood 2 :theme  2 :frightened  2 :competitiveness 1 }
             {:name "Nemica" :experience 1 :teamplayer 1 :adroit 3 :mood 2 :theme  2 :frightened  1 :competitiveness 2 }
             {:name "Roko" :experience 5 :teamplayer 2 :adroit 1 :mood 3 :theme  2 :frightened  2 :competitiveness 1 }
             {:name "Roki" :experience  4 :teamplayer 1 :adroit 4 :mood 1 :theme  2 :frightened  1 :competitiveness 2 }
             {:name"Teo" :experience  1 :teamplayer 1 :adroit 2 :mood 5 :theme  2 :frightened  1 :competitiveness 2 }
             {:name "Nelsi" :experience  3 :teamplayer 2 :adroit 3 :mood 4 :theme  1 :frightened  1 :competitiveness 2  }
             {:name "Beli" :experience  5 :teamplayer 2 :adroit 1 :mood 2 ::theme  1 :frightened  2 :competitiveness 1  }
             {:name "Cole" :experience  4 :teamplayer 1 :adroit 5 :mood 1 :theme  1 :frightened 1 :competitiveness 1  }
             {:name "Bebinger" :experience  1 :teamplayer 1 :adroit 4 :mood 5 :theme  2 :frightened  2 :competitiveness 1 }
             ])

(defn group-competitive-players [players]
  (let [grouped-players (group-by #(= (:competitiveness %) 1) players)]
    (concat (grouped-players true) (grouped-players false))))

(group-competitive-players igraci)

 
(def duzina (count igraci))
;duzina



(defn check-topic
  [igrac]
  (if (get igrac :theme)
    2
    0)
 )


(defn check-fear
  [igrac]
  (if (= 1 (get igrac :frightened)) 
   -1
    2))

(defn sum-good-attributes
  [igrac]
(+ (get igrac :mood) (get igrac :experience) (get igrac :adroit)))


(defn check-competitive
  [igrac]
  (if (= (get igrac :competitiveness) 1)
    -1
    1))

(defn check-team-player
  [igrac]
  (if (= (get igrac :teamplayer) 1)
    2
    0))

(defn sum-everything
  [igrac]
  (assoc igrac :summary (+ (sum-good-attributes igrac) (check-fear igrac) (check-topic igrac) (check-competitive igrac) (check-team-player igrac))))


(defn sort-everything
 [igraci duzina]
  (loop [i 0 sortiran []]
    (if (= duzina i)
      sortiran
   (recur (inc i) (sort-by :summary (my-into [(sum-everything (nth igraci i))] sortiran))))))


(defn team-added
  [tim i]
  (if (clojure.core/even? i)
    1
    2))

(defn give-teams
  [n lista]
  (loop [i 0 list-with-teams '()]
    (if (= i n )
      list-with-teams
      (recur (inc i) (my-into [(update (nth lista i) :tim team-added i)] list-with-teams))
      )
    ))
duzina
 


(defn pravljenjeEkipa
  [n lista]
  (let [sortirana (give-teams n (sort-everything lista n))] 
    (loop  [i 0 j 1 t1 #{(if (clojure.core/odd? n)
                           (get (last sortirana) :name)
                           "")} t2 #{}]
      (if (or (= n j) (= n i) (= n (+ (count t2) (count t2))))
        (println "evo prvog tima" t1 "  evo drugog tima" t2)
        (recur (+ 1 (inc i))
               (+ 1 (inc j))
               (my-into [(get (nth sortirana i) :name)] t1)
               (my-into [(get (nth sortirana j) :name)] t2))))))

;sada cu da pokusam da napravim algoritam za podelu u tri ekipe. 
;ideja mi je da u odnosu na ovaj za podelu na dva unapredim algoritam tako sto ce se 
;uzimati u obzir i kakva je soba. To cu da postignem tako sto 
;cu uvesti pojam koeficijenta igraca na koji ce razlicite vrednosti vezane za sobu razlicito
;ucitaci na vrednost atributa igraca tj doprinos svakog od njih konacnom koeficijentu

(defn calculate-coef [player room-data]
  (let [base-coef (+ (:experience player) (:adroit player) (:mood player))
        horror-adjust (cond
                        (and (= 1 (:horror room-data)) (= 1 (:frightened player))) -4
                        (and (= 1 (:horror room-data)) (= 2 (:frightened player))) -1
                        :else 0)
        knowledge-adjust (if (and (= 1 (:knowledge room-data)) (= 1 (:theme player))) 3 0)
        tech-adjust (if (and (= 1 (:tech room-data)) (> (:experience player) 3) (= 1 (:theme player))) 1 0)
        linear-adjust (if (and (= 2 (:linear room-data)) (= 1 (:teamplayer player))) 1 0)]
    (+ base-coef horror-adjust knowledge-adjust tech-adjust linear-adjust)))




(defn add-coefs-to-players [players room-data]
  (map (fn [player]
         (assoc player :coef (calculate-coef player room-data)))
       players))


(defn divide-players [players]
  (let [ensure-coef (fn [player] (if (nil? (:coef player)) (assoc player :coef 0) player))
        sorted-players (sort-by :coef > (map ensure-coef players))
        team-1 []
        team-2 []
        team-3 []
        teams (reduce (fn [[team1 team2 team3] player]
                        (let [team-sizes (map count [team1 team2 team3])
                              min-size (apply min team-sizes)
                              min-index (first (keep-indexed #(when (= %2 min-size) %1) team-sizes))]
                          (cond
                            (= min-index 0) [(conj team1 player) team2 team3]
                            (= min-index 1) [team1 (conj team2 player) team3]
                            :else [team1 team2 (conj team3 player)])))
                      [team-1 team-2 team-3]
                      sorted-players)]
    teams))


;ovo do sada je izostavilo uticaj toga da ti je igrac timski igrac ili kompetitivan
;jer ti faktori imaju znacaja tek nakon sto je inicijalna podela vec napravljena, zato sto sami od sebe
;nisu vazni koliko i kada se porede sa ostalima. Dakle, sada dodajem te faktore u algoritam tako sto
;timski igraci bolje igraju kada su zajedno, dok kompetitivni igraci nisu losiji ako su sami, ali losiji su
;ako su dva u istom timu. To cu implementirati preko uticaja na koeficijent
(defn adjust-coefs-for-traits [team]
  (let [teamplayer-count (count (filter #(= 1 (:teamplayer %)) team))
        competitive-count (count (filter #(= 1 (:competitiveness %)) team))]
    (map (fn [player]
           (let [teamplayer-adjust (if (> teamplayer-count 1) 1 0)
                 competitive-adjust (if (> competitive-count 1) -1 0)]
             (update player :coef + teamplayer-adjust competitive-adjust)))
         team)))



(defn calculate-threshold [room-data teams]
  (let [base-threshold 8
        team-size-factor (if (> (count (first teams)) 5) 1 0.8) ;; smaller threshold for smaller teams
        linear-factor (if (= 1 (:linear room-data)) 1.2 1) ;; higher threshold for linear rooms
        horror-factor (if (= 1 (:horror room-data)) 0.8 1)] ;; smaller threshold for horror rooms
    (int (* base-threshold team-size-factor linear-factor horror-factor))))






(defn is-balanced [teams threshold]
  (let [team-coefs (map #(reduce + (map :coef %)) teams)
        max-coef (apply max team-coefs)
        min-coef (apply min team-coefs)]
    (<= (- max-coef min-coef) threshold)))



(defn redivide-if-unbalanced [teams original-players threshold]
  (if (is-balanced teams threshold)
    teams
    (let [updated-players (apply concat (map adjust-coefs-for-traits teams))]
      (divide-players updated-players))))




(defn print-teams [balanced-teams]
  (let [format-team (fn [team]
                      (clojure.string/join ", " (map :name team)))]
    (loop [idx 0
           teams balanced-teams
           result ""]
      (if (empty? teams)
        result
        (let [team-str (format-team (first teams))
              new-result (str result "Team " (inc idx) ": " team-str "\n")]
          (recur (inc idx) (rest teams) new-result))))))


;sledecu funkciju pravim da bih lakse pozvala ceo ovaj algoritam iz drugog namespace-a
 (defn create-and-print-balanced-teams [players room-data]
  (let [players-with-coef (add-coefs-to-players players room-data)
        divided-teams (divide-players players-with-coef)
        threshold (calculate-threshold room-data divided-teams)
        balanced-teams (redivide-if-unbalanced divided-teams players-with-coef threshold)]
    (print-teams balanced-teams)))








