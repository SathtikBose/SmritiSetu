import { View, Text, SafeAreaView, ScrollView, TouchableOpacity } from 'react-native';
import { useRouter } from 'expo-router';

export default function GamesScreen() {
  const router = useRouter();
  const games = [
    { title: 'Memory Match', category: 'Memory', color: 'bg-indigo-500' },
    { title: 'Pattern Recall', category: 'Visual', color: 'bg-purple-500' },
    { title: 'Word Search', category: 'Language', color: 'bg-emerald-500' },
    { title: 'Number Sequence', category: 'Logic', color: 'bg-orange-500' }
  ];

  return (
    <SafeAreaView className="flex-1 bg-slate-950">
      <ScrollView className="flex-1 px-6 pt-8" showsVerticalScrollIndicator={false}>
        <Text className="text-3xl font-bold text-white mb-2">Games</Text>
        <Text className="text-slate-400 font-medium mb-8">Train your brain today</Text>

        <View className="flex-row flex-wrap justify-between">
          {games.map((game, index) => (
            <TouchableOpacity 
              key={index} 
              className="w-[48%] bg-white/5 border border-white/10 rounded-3xl p-5 mb-4 shadow-lg active:scale-95 transition-transform"
              onPress={() => {
                if (game.title === 'Memory Match') {
                  router.push('/games/memory' as any);
                }
              }}
            >
              <View className={`${game.color} w-12 h-12 rounded-2xl mb-4 opacity-80`} />
              <Text className="text-white font-bold text-lg mb-1">{game.title}</Text>
              <Text className="text-slate-400 text-sm">{game.category}</Text>
            </TouchableOpacity>
          ))}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}
