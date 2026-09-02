import { View, Text, TouchableOpacity, SafeAreaView } from 'react-native';
import { useRouter } from 'expo-router';
import { ArrowLeft, User } from 'lucide-react-native';

export default function ProfileScreen() {
  const router = useRouter();

  return (
    <SafeAreaView className="flex-1 bg-slate-950">
      <View className="flex-1 px-6 pt-8">
        <View className="flex-row items-center mb-8">
          <TouchableOpacity 
            className="w-10 h-10 bg-white/10 rounded-full items-center justify-center border border-white/10 mr-4"
            onPress={() => router.back()}
          >
            <ArrowLeft color="#fff" size={20} />
          </TouchableOpacity>
          <Text className="text-3xl font-bold text-white">Profile</Text>
        </View>
        
        <View className="bg-white/10 border border-white/20 p-8 rounded-3xl items-center shadow-xl mb-6">
          <View className="w-24 h-24 bg-indigo-500/30 rounded-full items-center justify-center mb-4 border-2 border-indigo-400">
            <User color="#818cf8" size={40} />
          </View>
          <Text className="text-2xl font-bold text-white mb-1">Jane Doe</Text>
          <Text className="text-slate-400 font-medium mb-6">jane.doe@example.com</Text>
          
          <View className="w-full flex-row justify-between bg-slate-900/50 p-4 rounded-2xl border border-slate-700">
            <View className="items-center">
              <Text className="text-white font-bold text-xl">42</Text>
              <Text className="text-slate-400 text-sm">Games</Text>
            </View>
            <View className="w-px bg-slate-700" />
            <View className="items-center">
              <Text className="text-white font-bold text-xl">Gold</Text>
              <Text className="text-slate-400 text-sm">League</Text>
            </View>
            <View className="w-px bg-slate-700" />
            <View className="items-center">
              <Text className="text-white font-bold text-xl">1.2k</Text>
              <Text className="text-slate-400 text-sm">Points</Text>
            </View>
          </View>
        </View>
        
        <TouchableOpacity 
          className="bg-indigo-500 w-full py-4 rounded-xl items-center shadow-lg shadow-indigo-500/30"
          onPress={() => router.push('/settings/edit-profile' as any)}
        >
          <Text className="text-white font-bold text-lg">Edit Profile</Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
}
