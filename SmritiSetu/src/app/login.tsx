import { View, Text, TouchableOpacity } from 'react-native';
import { useRouter } from 'expo-router';

export default function LoginScreen() {
  const router = useRouter();

  return (
    <View className="flex-1 items-center justify-center bg-slate-900">
      <Text className="text-3xl font-bold text-white mb-8">Login</Text>
      
      <TouchableOpacity 
        className="bg-indigo-500 px-8 py-4 rounded-xl w-64 mb-4 items-center"
        onPress={() => router.replace('/(tabs)')}
      >
        <Text className="text-white font-semibold">Login</Text>
      </TouchableOpacity>
      
      <TouchableOpacity 
        className="bg-slate-800 px-8 py-4 rounded-xl w-64 mb-4 items-center"
        onPress={() => router.push('/signup')}
      >
        <Text className="text-white font-semibold">Sign Up</Text>
      </TouchableOpacity>
      
      <TouchableOpacity 
        className="px-8 py-4 w-64 items-center"
        onPress={() => router.push('/forgot-password')}
      >
        <Text className="text-indigo-400 font-semibold">Forgot Password?</Text>
      </TouchableOpacity>
    </View>
  );
}
