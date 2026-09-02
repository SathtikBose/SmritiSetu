import { useCallback } from 'react';

export function useAudio(soundEnabled: boolean, musicEnabled: boolean) {
  const playSound = useCallback((type: string) => {
    // Sound features have been temporarily removed to fix the ExponentAV crash.
  }, [soundEnabled, musicEnabled]);

  return { playSound };
}
