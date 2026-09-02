import React, { createContext, useContext, useState, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';

type ThemeType = 'default' | 'space' | 'animals';

interface MemoryGameContextType {
  soundEnabled: boolean;
  musicEnabled: boolean;
  currentLevel: number;
  theme: ThemeType;
  toggleSound: () => void;
  toggleMusic: () => void;
  setTheme: (theme: ThemeType) => void;
  advanceLevel: () => void;
  resetGame: () => void;
  isReady: boolean;
}

const MemoryGameContext = createContext<MemoryGameContextType | undefined>(undefined);

const STORAGE_KEY = '@smritisetu_memory_settings_v1';

export function MemoryGameProvider({ children }: { children: React.ReactNode }) {
  const [soundEnabled, setSoundEnabled] = useState(true);
  const [musicEnabled, setMusicEnabled] = useState(true);
  const [currentLevel, setCurrentLevel] = useState(1);
  const [theme, setThemeState] = useState<ThemeType>('default');
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    loadSettings();
  }, []);

  const loadSettings = async () => {
    try {
      const stored = await AsyncStorage.getItem(STORAGE_KEY);
      if (stored) {
        const parsed = JSON.parse(stored);
        setSoundEnabled(parsed.soundEnabled ?? true);
        setMusicEnabled(parsed.musicEnabled ?? true);
        setCurrentLevel(parsed.currentLevel ?? 1);
        setThemeState(parsed.theme ?? 'default');
      }
    } catch (e) {
      console.warn('Failed to load memory game settings', e);
    } finally {
      setIsReady(true);
    }
  };

  const saveSettings = async (updates: any) => {
    try {
      const currentState = { soundEnabled, musicEnabled, currentLevel, theme };
      const newState = { ...currentState, ...updates };
      await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(newState));
    } catch (e) {
      console.warn('Failed to save memory game settings', e);
    }
  };

  const toggleSound = () => {
    setSoundEnabled(prev => {
      saveSettings({ soundEnabled: !prev });
      return !prev;
    });
  };

  const toggleMusic = () => {
    setMusicEnabled(prev => {
      saveSettings({ musicEnabled: !prev });
      return !prev;
    });
  };

  const setTheme = (newTheme: ThemeType) => {
    setThemeState(newTheme);
    saveSettings({ theme: newTheme });
  };

  const advanceLevel = () => {
    setCurrentLevel(prev => {
      const next = Math.min(prev + 1, 10);
      saveSettings({ currentLevel: next });
      return next;
    });
  };

  const resetGame = () => {
    setCurrentLevel(1);
    saveSettings({ currentLevel: 1 });
  };

  return (
    <MemoryGameContext.Provider
      value={{
        soundEnabled,
        musicEnabled,
        currentLevel,
        theme,
        toggleSound,
        toggleMusic,
        setTheme,
        advanceLevel,
        resetGame,
        isReady,
      }}
    >
      {children}
    </MemoryGameContext.Provider>
  );
}

export function useMemoryGame() {
  const context = useContext(MemoryGameContext);
  if (context === undefined) {
    throw new Error('useMemoryGame must be used within a MemoryGameProvider');
  }
  return context;
}
