import { View, Text, TouchableOpacity } from 'react-native';
import { useRouter } from 'expo-router';

export default function ProfileScreen() {
  const router = useRouter();

  return (
    <View className="flex-1 items-center justify-center bg-slate-900">
      <Text className="text-3xl font-bold text-white mb-8">Profile</Text>
      
      <TouchableOpacity 
        className="bg-indigo-500 px-8 py-4 rounded-xl w-64 items-center"
        onPress={() => router.back()}
      >
        <Text className="text-white font-semibold">Back to Settings</Text>
      </TouchableOpacity>
    </View>
  );
}
