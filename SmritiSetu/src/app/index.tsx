import { View, Text, TouchableOpacity } from 'react-native';
import { useRouter } from 'expo-router';

export default function SplashScreen() {
  const router = useRouter();

  return (
    <View className="flex-1 items-center justify-center bg-slate-900">
      <Text className="text-4xl font-bold text-indigo-400 mb-8">SmritiSetu</Text>
      <TouchableOpacity 
        className="bg-indigo-500 px-8 py-4 rounded-xl"
        onPress={() => router.push('/login')}
      >
        <Text className="text-white font-semibold text-lg">Get Started</Text>
      </TouchableOpacity>
    </View>
  );
}
