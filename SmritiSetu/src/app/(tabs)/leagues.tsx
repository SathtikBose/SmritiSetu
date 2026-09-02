import { View, Text, SafeAreaView, ScrollView } from 'react-native';
import { Trophy } from 'lucide-react-native';

export default function LeaguesScreen() {
  const leaderboard = [
    { rank: 1, name: 'Alex Johnson', points: '2,450', isMe: false },
    { rank: 2, name: 'Sarah Williams', points: '2,100', isMe: false },
    { rank: 3, name: 'Jane Doe', points: '1,200', isMe: true },
    { rank: 4, name: 'Mike Brown', points: '980', isMe: false },
    { rank: 5, name: 'Emily Chen', points: '850', isMe: false },
  ];

  return (
    <SafeAreaView className="flex-1 bg-slate-950">
      <View className="flex-1 px-6 pt-8">
        <Text className="text-3xl font-bold text-white mb-2">Leagues</Text>
        <Text className="text-slate-400 font-medium mb-8">Gold League - Week 4</Text>

        <View className="bg-amber-500/20 border border-amber-500/30 p-6 rounded-3xl mb-8 flex-row justify-between items-center">
          <View>
            <Text className="text-amber-200 font-bold text-lg mb-1">Current Standing</Text>
            <Text className="text-white text-sm">Top 30% advance to Diamond</Text>
          </View>
          <Trophy color="#f59e0b" size={32} />
        </View>

        <Text className="text-xl font-bold text-white mb-4">Leaderboard</Text>
        <ScrollView showsVerticalScrollIndicator={false}>
          <View className="bg-white/10 border border-white/10 rounded-3xl overflow-hidden mb-6">
            {leaderboard.map((user, index) => (
              <View 
                key={index} 
                className={`p-5 flex-row items-center justify-between ${
                  index !== leaderboard.length - 1 ? 'border-b border-white/5' : ''
                } ${user.isMe ? 'bg-indigo-500/20' : ''}`}
              >
                <View className="flex-row items-center">
                  <Text className={`font-bold w-6 ${user.rank <= 3 ? 'text-amber-400' : 'text-slate-400'}`}>
                    #{user.rank}
                  </Text>
                  <View className="w-10 h-10 bg-slate-800 rounded-full mx-3" />
                  <Text className={`font-semibold ${user.isMe ? 'text-indigo-300' : 'text-white'}`}>
                    {user.name}
                  </Text>
                </View>
                <Text className="text-white font-bold">{user.points}</Text>
              </View>
            ))}
          </View>
        </ScrollView>
      </View>
    </SafeAreaView>
  );
}
