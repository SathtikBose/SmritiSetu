import { View, Text, TouchableOpacity, SafeAreaView, KeyboardAvoidingView, Platform, ScrollView } from 'react-native';
import { useRouter } from 'expo-router';

export default function SplashScreen() {
  const router = useRouter();

  return (
    <SafeAreaView className="flex-1 bg-slate-950">
      <View className="flex-1 items-center justify-center px-6">
        <View className="items-center justify-center w-full h-full">
          <View className="bg-indigo-500/20 w-48 h-48 rounded-full absolute blur-3xl opacity-50" />
          <View className="bg-purple-500/20 w-48 h-48 rounded-full absolute blur-3xl opacity-50 top-1/4 left-1/4" />
          
          <View className="w-full bg-white/10 border border-white/20 rounded-3xl p-8 items-center z-10 shadow-lg">
            <View className="bg-indigo-500/20 p-4 rounded-full mb-6 border border-indigo-400/30">
              <Text className="text-5xl">✨</Text>
            </View>
            <Text className="text-4xl font-bold text-white mb-2 tracking-tight">SmritiSetu</Text>
            <Text className="text-slate-300 text-center mb-8 text-base font-medium">Connect. Play. Remember.</Text>
            
            <TouchableOpacity 
              className="bg-indigo-500 w-full py-4 rounded-xl flex-row justify-center items-center shadow-lg shadow-indigo-500/30 active:scale-95 transition-transform"
              onPress={() => router.push('/login')}
            >
              <Text className="text-white font-bold text-lg">Get Started</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </SafeAreaView>
  );
}
