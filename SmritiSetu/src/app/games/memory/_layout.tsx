import { Stack } from 'expo-router';
import { MemoryGameProvider } from '../../../context/MemoryGameContext';

export default function MemoryGameLayout() {
  return (
    <MemoryGameProvider>
      <Stack screenOptions={{ headerShown: false, animation: 'fade' }}>
        <Stack.Screen name="index" />
        <Stack.Screen name="game" />
        <Stack.Screen name="settings" />
      </Stack>
    </MemoryGameProvider>
  );
}
