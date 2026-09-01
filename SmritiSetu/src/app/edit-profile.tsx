import { View, Text, TextInput, TouchableOpacity, ScrollView } from 'react-native';
import { useRouter } from 'expo-router';
import { ArrowLeft, Camera } from 'lucide-react-native';

export default function EditProfile() {
  const router = useRouter();

  return (
    <View className="flex-1 bg-slate-900">
      <View className="flex-row items-center justify-between px-6 pt-16 pb-4 bg-slate-800">
        <TouchableOpacity onPress={() => router.back()} className="h-10 w-10 justify-center">
          <ArrowLeft color="#818cf8" size={24} />
        </TouchableOpacity>
        <Text className="text-xl font-bold text-slate-100">Edit Profile</Text>
        <View className="w-10" />
      </View>

      <ScrollView className="flex-1 px-6 pt-8">
        <View className="items-center mb-10">
          <View className="h-24 w-24 rounded-full bg-slate-700 items-center justify-center overflow-hidden border-2 border-indigo-500 relative">
            <Text className="text-3xl text-indigo-300 font-bold">JD</Text>
            <View className="absolute bottom-0 w-full bg-black/50 py-1 items-center">
              <Camera color="#e2e8f0" size={14} />
            </View>
          </View>
        </View>

        <View className="space-y-6">
          <View>
            <Text className="text-slate-300 mb-2">Full Name</Text>
            <TextInput 
              className="w-full bg-slate-800 text-slate-100 px-4 py-3 rounded-lg border border-slate-700"
              defaultValue="John Doe"
              placeholderTextColor="#64748b"
            />
          </View>

          <View>
            <Text className="text-slate-300 mb-2">Email</Text>
            <TextInput 
              className="w-full bg-slate-800 text-slate-100 px-4 py-3 rounded-lg border border-slate-700"
              defaultValue="johndoe@example.com"
              keyboardType="email-address"
              placeholderTextColor="#64748b"
            />
          </View>
          
          <View>
            <Text className="text-slate-300 mb-2">Location</Text>
            <TextInput 
              className="w-full bg-slate-800 text-slate-100 px-4 py-3 rounded-lg border border-slate-700"
              defaultValue="New York, USA"
              placeholderTextColor="#64748b"
            />
          </View>

          <TouchableOpacity 
            className="w-full bg-indigo-500 py-3 rounded-lg items-center mt-6"
            onPress={() => router.back()}
          >
            <Text className="text-white font-semibold text-lg">Save Changes</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </View>
  );
}
