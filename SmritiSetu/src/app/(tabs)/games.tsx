import { Text, View } from "react-native";
import { Gamepad2 } from "lucide-react-native";

export default function GamesScreen() {
  return (
    <View className="flex-1 items-center justify-center bg-slate-900 p-4">
      <Gamepad2 color="#818cf8" size={64} />
      <Text className="text-2xl font-bold text-indigo-400 mt-4">Games</Text>
      <Text className="text-base text-slate-300 mt-2">
        Play and train your memory
      </Text>
    </View>
  );
}
