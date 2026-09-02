import React, { useState, useEffect, useCallback, useRef } from 'react';
import { View, Text, TouchableOpacity, Dimensions, Modal } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { ChevronLeft, Pause, Play, RotateCcw } from 'lucide-react-native';
import { useMemoryGame } from '../../../context/MemoryGameContext';
import { useAudio } from '../../../hooks/useAudio';
import { MemoryCard, CardData } from '../../../components/MemoryCard';

const { width } = Dimensions.get('window');

const getLevelConfig = (level: number) => {
  // Max level 10
  const pairs = Math.min(8 + Math.floor(level / 2) * 2, 18); 
  const cols = pairs <= 10 ? 4 : (pairs <= 15 ? 5 : 6);
  const timeLimit = 60 + (level * 10);
  return { pairs, cols, timeLimit };
};

export default function MemoryGameScreen() {
  const router = useRouter();
  const { currentLevel, advanceLevel, soundEnabled, musicEnabled } = useMemoryGame();
  const { playSound } = useAudio(soundEnabled, musicEnabled);
  
  const config = getLevelConfig(currentLevel);
  const padding = 16;
  const cardSize = (width - (padding * 2) - (config.cols * 8)) / config.cols;

  const [cards, setCards] = useState<CardData[]>([]);
  const [firstSelection, setFirstSelection] = useState<number | null>(null);
  const [mismatchIds, setMismatchIds] = useState<number[]>([]);
  
  const [moves, setMoves] = useState(0);
  const [matches, setMatches] = useState(0);
  const [timeLeft, setTimeLeft] = useState(config.timeLimit);
  
  const [isPaused, setIsPaused] = useState(false);
  const [isGameOver, setIsGameOver] = useState(false);
  const [hasWon, setHasWon] = useState(false);

  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const lockBoard = useRef(false);

  const initializeGame = useCallback(() => {
    const newCards: CardData[] = [];
    for (let i = 0; i < config.pairs; i++) {
      newCards.push({ id: i * 2, iconId: i, state: 'down' });
      newCards.push({ id: i * 2 + 1, iconId: i, state: 'down' });
    }
    // Shuffle
    newCards.sort(() => Math.random() - 0.5);
    setCards(newCards);
    setMoves(0);
    setMatches(0);
    setTimeLeft(config.timeLimit);
    setFirstSelection(null);
    setMismatchIds([]);
    setIsGameOver(false);
    setHasWon(false);
    setIsPaused(false);
    lockBoard.current = false;
  }, [config.pairs, config.timeLimit]);

  useEffect(() => {
    initializeGame();
  }, [currentLevel, initializeGame]);

  useEffect(() => {
    if (!isPaused && !isGameOver) {
      timerRef.current = setInterval(() => {
        setTimeLeft((prev) => {
          if (prev <= 1) {
            handleGameOver(false);
            return 0;
          }
          if (prev === 11) playSound('tick'); // Warning tick
          return prev - 1;
        });
      }, 1000);
    }
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [isPaused, isGameOver, playSound]);

  const handleGameOver = (win: boolean) => {
    if (timerRef.current) clearInterval(timerRef.current);
    setIsGameOver(true);
    setHasWon(win);
    playSound(win ? 'win' : 'mismatch');
  };

  const handleCardPress = (id: number) => {
    if (lockBoard.current || isPaused || isGameOver) return;
    
    const cardIndex = cards.findIndex(c => c.id === id);
    if (cards[cardIndex].state !== 'down') return;

    playSound('flip');
    
    const newCards = [...cards];
    newCards[cardIndex] = { ...newCards[cardIndex], state: 'up' };
    setCards(newCards);

    if (firstSelection === null) {
      setFirstSelection(id);
    } else {
      lockBoard.current = true;
      setMoves(m => m + 1);
      
      const firstIndex = cards.findIndex(c => c.id === firstSelection);
      if (newCards[firstIndex].iconId === newCards[cardIndex].iconId) {
        // Match
        playSound('match');
        newCards[firstIndex] = { ...newCards[firstIndex], state: 'matched' };
        newCards[cardIndex] = { ...newCards[cardIndex], state: 'matched' };
        setCards(newCards);
        setFirstSelection(null);
        setMatches(m => {
          const newMatches = m + 1;
          if (newMatches === config.pairs) {
            setTimeout(() => handleGameOver(true), 500);
          }
          return newMatches;
        });
        lockBoard.current = false;
      } else {
        // Mismatch
        setMismatchIds([firstSelection, id]);
        setTimeout(() => {
          playSound('mismatch');
          setCards(prev => {
            const revertCards = [...prev];
            revertCards[firstIndex] = { ...revertCards[firstIndex], state: 'down' };
            revertCards[cardIndex] = { ...revertCards[cardIndex], state: 'down' };
            return revertCards;
          });
          setFirstSelection(null);
          setMismatchIds([]);
          lockBoard.current = false;
        }, 800);
      }
    }
  };

  return (
    <SafeAreaView className="flex-1 bg-slate-950">
      <View className="px-4 pt-4 flex-row justify-between items-center z-10">
        <TouchableOpacity 
          className="w-10 h-10 bg-white/10 rounded-full items-center justify-center border border-white/20"
          onPress={() => {
            playSound('click');
            router.back();
          }}
        >
          <ChevronLeft color="white" size={20} />
        </TouchableOpacity>
        
        <View className="bg-white/10 px-4 py-2 rounded-full border border-white/20">
          <Text className="text-white font-bold text-lg">Level {currentLevel}</Text>
        </View>

        <TouchableOpacity 
          className="w-10 h-10 bg-white/10 rounded-full items-center justify-center border border-white/20"
          onPress={() => {
            playSound('click');
            setIsPaused(!isPaused);
          }}
        >
          {isPaused ? <Play color="white" size={20} /> : <Pause color="white" size={20} />}
        </TouchableOpacity>
      </View>

      <View className="flex-row justify-around py-6 z-10">
        <View className="items-center">
          <Text className="text-slate-400 font-medium mb-1">Time</Text>
          <Text className={`text-2xl font-bold ${timeLeft <= 10 ? 'text-red-400' : 'text-white'}`}>
            0:{timeLeft.toString().padStart(2, '0')}
          </Text>
        </View>
        <View className="items-center">
          <Text className="text-slate-400 font-medium mb-1">Moves</Text>
          <Text className="text-2xl font-bold text-white">{moves}</Text>
        </View>
      </View>

      <View className="flex-1 items-center justify-center px-4">
        <View 
          className="flex-row flex-wrap justify-center" 
          style={{ width: width - 16 }}
        >
          {cards.map(card => (
            <MemoryCard 
              key={card.id}
              card={card}
              size={cardSize}
              onPress={handleCardPress}
              mismatchShake={mismatchIds.includes(card.id)}
            />
          ))}
        </View>
      </View>

      {/* Game Over Modal */}
      <Modal visible={isGameOver || isPaused} transparent animationType="fade">
        <View className="flex-1 bg-slate-950/80 items-center justify-center px-6">
          <View className="bg-slate-900 border border-slate-700 p-8 rounded-3xl w-full items-center shadow-2xl">
            <Text className={`text-4xl font-extrabold mb-2 ${hasWon ? 'text-emerald-400' : 'text-amber-400'}`}>
              {isPaused ? 'Paused' : hasWon ? 'You Win!' : 'Time\'s Up!'}
            </Text>
            
            {!isPaused && (
              <View className="bg-white/5 border border-white/10 w-full rounded-2xl p-4 my-6">
                <View className="flex-row justify-between mb-2">
                  <Text className="text-slate-400 font-medium">Time Left:</Text>
                  <Text className="text-white font-bold">{timeLeft}s</Text>
                </View>
                <View className="flex-row justify-between">
                  <Text className="text-slate-400 font-medium">Moves:</Text>
                  <Text className="text-white font-bold">{moves}</Text>
                </View>
              </View>
            )}

            {hasWon ? (
              <TouchableOpacity 
                className="bg-emerald-500 w-full py-4 rounded-xl items-center mb-4"
                onPress={() => {
                  playSound('click');
                  advanceLevel();
                }}
              >
                <Text className="text-white font-bold text-lg">Next Level</Text>
              </TouchableOpacity>
            ) : isPaused ? (
              <TouchableOpacity 
                className="bg-indigo-500 w-full py-4 rounded-xl items-center mb-4 flex-row justify-center"
                onPress={() => {
                  playSound('click');
                  setIsPaused(false);
                }}
              >
                <Play color="white" size={20} />
                <Text className="text-white font-bold text-lg ml-2">Resume</Text>
              </TouchableOpacity>
            ) : null}

            <TouchableOpacity 
              className="bg-white/10 border border-white/20 w-full py-4 rounded-xl items-center flex-row justify-center"
              onPress={() => {
                playSound('click');
                if (isPaused) {
                  setIsPaused(false);
                  router.back();
                } else {
                  initializeGame();
                }
              }}
            >
              <RotateCcw color="white" size={20} />
              <Text className="text-white font-bold text-lg ml-2">
                {isPaused ? 'Quit' : 'Try Again'}
              </Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>

    </SafeAreaView>
  );
}
