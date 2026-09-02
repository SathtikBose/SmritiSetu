import { View, Text, SafeAreaView, ScrollView } from 'react-native';

export default function HomeScreen() {
  return (
    <SafeAreaView className="flex-1 bg-slate-950">
      <ScrollView className="flex-1 px-6 pt-8" showsVerticalScrollIndicator={false}>
        <View className="flex-row justify-between items-center mb-8">
          <View>
            <Text className="text-slate-400 font-medium">Good Morning,</Text>
            <Text className="text-3xl font-bold text-white">Jane Doe 👋</Text>
          </View>
          <View className="w-12 h-12 bg-indigo-500/30 rounded-full border border-indigo-500/50" />
        </View>

        <View className="bg-indigo-500/20 border border-indigo-500/30 p-6 rounded-3xl mb-8 relative overflow-hidden">
          <View className="absolute -right-4 -top-4 w-32 h-32 bg-indigo-500/30 rounded-full blur-2xl" />
          <Text className="text-indigo-200 font-medium mb-1">Daily Streak</Text>
          <Text className="text-4xl font-bold text-white mb-2">12 Days</Text>
          <Text className="text-indigo-100">Keep it up! Play a game today to maintain your streak.</Text>
        </View>

        <Text className="text-xl font-bold text-white mb-4">Continue Playing</Text>
        <View className="bg-white/10 border border-white/10 p-6 rounded-3xl mb-4">
          <Text className="text-white font-bold text-lg mb-1">Memory Match</Text>
          <Text className="text-slate-400 mb-4">Level 4 • 80% Complete</Text>
          <View className="w-full bg-slate-800 h-2 rounded-full overflow-hidden">
            <View className="w-4/5 bg-indigo-500 h-full" />
          </View>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}
