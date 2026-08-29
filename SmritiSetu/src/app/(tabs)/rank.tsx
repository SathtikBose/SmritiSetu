import { Text, View } from "react-native";
import { Trophy } from "lucide-react-native";

export default function RankScreen() {
  return (
    <View className="flex-1 items-center justify-center bg-slate-900 p-4">
      <Trophy color="#818cf8" size={64} />
      <Text className="text-2xl font-bold text-indigo-400 mt-4">Rank</Text>
      <Text className="text-base text-slate-300 mt-2">
        Leaderboard and statistics
      </Text>
    </View>
  );
}
