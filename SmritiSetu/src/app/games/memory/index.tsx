import { View, Text, TouchableOpacity } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { Play, Settings, Trophy, ChevronLeft } from 'lucide-react-native';
import { useMemoryGame } from '../../../context/MemoryGameContext';
import { useAudio } from '../../../hooks/useAudio';

export default function MemoryMainMenu() {
  const router = useRouter();
  const { currentLevel, soundEnabled, musicEnabled } = useMemoryGame();
  const { playSound } = useAudio(soundEnabled, musicEnabled);

  const handleNav = (route: string) => {
    playSound('click');
    router.push(route as any);
  };

  const handleBack = () => {
    playSound('click');
    router.back();
  };

  return (
    <SafeAreaView className="flex-1 bg-slate-950">
      <View className="flex-1 justify-center px-8 relative">
        <TouchableOpacity 
          className="absolute top-12 left-6 w-12 h-12 bg-white/10 rounded-full items-center justify-center border border-white/20 z-10"
          onPress={handleBack}
        >
          <ChevronLeft color="white" size={24} />
        </TouchableOpacity>

        <View className="absolute top-20 right-10 w-64 h-64 bg-indigo-500/20 rounded-full blur-3xl opacity-40" />
        <View className="absolute bottom-20 left-10 w-64 h-64 bg-emerald-500/20 rounded-full blur-3xl opacity-40" />
        
        <View className="items-center mb-16">
          <View className="w-24 h-24 bg-indigo-500 rounded-3xl items-center justify-center mb-6 shadow-lg shadow-indigo-500/50">
            <Trophy color="white" size={48} />
          </View>
          <Text className="text-4xl font-extrabold text-white mb-2 tracking-tight">Memory Match</Text>
          <Text className="text-slate-400 text-lg">Train your brain, beat the levels.</Text>
          <View className="mt-4 bg-white/10 px-4 py-2 rounded-full border border-white/20">
            <Text className="text-indigo-300 font-bold">Current Level: {currentLevel} / 10</Text>
          </View>
        </View>

        <View className="space-y-4 gap-4">
          <TouchableOpacity 
            className="bg-indigo-500 flex-row items-center justify-center py-5 rounded-2xl shadow-lg shadow-indigo-500/30 active:scale-95 transition-transform"
            onPress={() => handleNav('/games/memory/game')}
          >
            <Play color="white" size={24} />
            <Text className="text-white font-bold text-xl ml-3">Play Level {currentLevel}</Text>
          </TouchableOpacity>
          
          <TouchableOpacity 
            className="bg-white/10 border border-white/20 flex-row items-center justify-center py-5 rounded-2xl active:scale-95 transition-transform"
            onPress={() => handleNav('/games/memory/settings')}
          >
            <Settings color="white" size={24} />
            <Text className="text-white font-bold text-lg ml-3">Settings</Text>
          </TouchableOpacity>
        </View>
      </View>
    </SafeAreaView>
  );
}
