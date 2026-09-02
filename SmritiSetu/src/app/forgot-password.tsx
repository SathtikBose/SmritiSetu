import { View, Text, TouchableOpacity } from 'react-native';
import { useRouter } from 'expo-router';

export default function ForgotPasswordScreen() {
  const router = useRouter();

  return (
    <View className="flex-1 items-center justify-center bg-slate-900">
      <Text className="text-3xl font-bold text-white mb-8">Forgot Password</Text>
      
      <TouchableOpacity 
        className="bg-indigo-500 px-8 py-4 rounded-xl w-64 mb-4 items-center"
        onPress={() => router.push('/otp')}
      >
        <Text className="text-white font-semibold">Send OTP</Text>
      </TouchableOpacity>
      
      <TouchableOpacity 
        className="px-8 py-4 w-64 items-center"
        onPress={() => router.back()}
      >
        <Text className="text-indigo-400 font-semibold">Back to Login</Text>
      </TouchableOpacity>
    </View>
  );
}
