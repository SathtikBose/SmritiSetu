import { View, Text, TouchableOpacity, Switch } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { ChevronLeft, Volume2, Music, RotateCcw } from 'lucide-react-native';
import { useMemoryGame } from '../../../context/MemoryGameContext';
import { useAudio } from '../../../hooks/useAudio';

export default function MemorySettings() {
  const router = useRouter();
  const { soundEnabled, musicEnabled, toggleSound, toggleMusic, resetGame, currentLevel } = useMemoryGame();
  const { playSound } = useAudio(soundEnabled, musicEnabled);

  const handleBack = () => {
    playSound('click');
    router.back();
  };

  const handleReset = () => {
    playSound('click');
    resetGame();
  };

  return (
    <SafeAreaView className="flex-1 bg-slate-950">
      <View className="flex-1 px-6 pt-8">
        <View className="flex-row items-center mb-8">
          <TouchableOpacity 
            className="w-12 h-12 bg-white/10 rounded-full items-center justify-center border border-white/20"
            onPress={handleBack}
          >
            <ChevronLeft color="white" size={24} />
          </TouchableOpacity>
          <Text className="text-2xl font-bold text-white ml-4">Game Settings</Text>
        </View>

        <View className="bg-white/10 border border-white/20 rounded-3xl overflow-hidden mb-8">
          <View className="p-5 flex-row items-center justify-between border-b border-white/5">
            <View className="flex-row items-center">
              <View className="w-10 h-10 bg-indigo-500/20 rounded-full items-center justify-center mr-4">
                <Volume2 color="#a5b4fc" size={20} />
              </View>
              <Text className="text-white font-medium text-lg">Sound Effects</Text>
            </View>
            <Switch 
              value={soundEnabled}
              onValueChange={() => {
                toggleSound();
                if (!soundEnabled) playSound('click');
              }}
              trackColor={{ false: '#334155', true: '#6366f1' }}
            />
          </View>
          
          <View className="p-5 flex-row items-center justify-between">
            <View className="flex-row items-center">
              <View className="w-10 h-10 bg-purple-500/20 rounded-full items-center justify-center mr-4">
                <Music color="#d8b4fe" size={20} />
              </View>
              <Text className="text-white font-medium text-lg">Background Music</Text>
            </View>
            <Switch 
              value={musicEnabled}
              onValueChange={toggleMusic}
              trackColor={{ false: '#334155', true: '#a855f7' }}
            />
          </View>
        </View>

        <View className="bg-red-500/10 border border-red-500/20 rounded-3xl overflow-hidden">
          <TouchableOpacity 
            className="p-5 flex-row items-center justify-between"
            onPress={handleReset}
          >
            <View className="flex-row items-center">
              <View className="w-10 h-10 bg-red-500/20 rounded-full items-center justify-center mr-4">
                <RotateCcw color="#fca5a5" size={20} />
              </View>
              <View>
                <Text className="text-white font-medium text-lg">Reset Progress</Text>
                <Text className="text-red-300 text-sm">You are on Level {currentLevel}</Text>
              </View>
            </View>
          </TouchableOpacity>
        </View>

      </View>
    </SafeAreaView>
  );
}
