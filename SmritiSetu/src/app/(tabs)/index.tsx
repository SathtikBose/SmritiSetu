import { Text, View } from "react-native";
import { Home } from "lucide-react-native";

export default function HomeScreen() {
  return (
    <View className="flex-1 items-center justify-center bg-slate-900 p-4">
      <Home color="#818cf8" size={64} />
      <Text className="text-2xl font-bold text-indigo-400 mt-4">Home</Text>
      <Text className="text-base text-slate-300 mt-2">
        Welcome to SmritiSetu
      </Text>
    </View>
  );
}
