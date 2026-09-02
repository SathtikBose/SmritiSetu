import { View, Text, TouchableOpacity } from 'react-native';
import { useRouter } from 'expo-router';

export default function ResetPasswordScreen() {
  const router = useRouter();

  return (
    <View className="flex-1 items-center justify-center bg-slate-900">
      <Text className="text-3xl font-bold text-white mb-8">Reset Password</Text>
      
      <TouchableOpacity 
        className="bg-indigo-500 px-8 py-4 rounded-xl w-64 mb-4 items-center"
        onPress={() => router.replace('/login')}
      >
        <Text className="text-white font-semibold">Confirm Reset</Text>
      </TouchableOpacity>
    </View>
  );
}
