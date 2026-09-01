import { View, Text, TouchableOpacity } from 'react-native';
import { Link, useRouter } from 'expo-router';
import { ArrowLeft, Edit3, Mail, MapPin } from 'lucide-react-native';

export default function Profile() {
  const router = useRouter();

  return (
    <View className="flex-1 bg-slate-900">
      <View className="flex-row items-center justify-between px-6 pt-16 pb-4 bg-slate-800">
        <TouchableOpacity onPress={() => router.back()} className="h-10 w-10 justify-center">
          <ArrowLeft color="#818cf8" size={24} />
        </TouchableOpacity>
        <Text className="text-xl font-bold text-slate-100">Profile</Text>
        {/* @ts-ignore */}
        <Link href="/edit-profile" asChild>
          <TouchableOpacity className="h-10 w-10 justify-center items-end">
            <Edit3 color="#818cf8" size={20} />
          </TouchableOpacity>
        </Link>
      </View>

      <View className="items-center mt-8">
        <View className="h-24 w-24 rounded-full bg-slate-700 items-center justify-center overflow-hidden mb-4 border-2 border-indigo-500">
          <Text className="text-3xl text-indigo-300 font-bold">JD</Text>
        </View>
        <Text className="text-2xl font-bold text-slate-100">John Doe</Text>
        <Text className="text-slate-400 mt-1">Memory Champion</Text>
      </View>

      <View className="px-6 mt-10 space-y-6">
        <View className="flex-row items-center border-b border-slate-800 pb-4">
          <View className="h-10 w-10 rounded-full bg-slate-800 items-center justify-center mr-4">
            <Mail color="#94a3b8" size={20} />
          </View>
          <View>
            <Text className="text-sm text-slate-400">Email</Text>
            <Text className="text-slate-200 font-medium mt-1">johndoe@example.com</Text>
          </View>
        </View>

        <View className="flex-row items-center border-b border-slate-800 pb-4">
          <View className="h-10 w-10 rounded-full bg-slate-800 items-center justify-center mr-4">
            <MapPin color="#94a3b8" size={20} />
          </View>
          <View>
            <Text className="text-sm text-slate-400">Location</Text>
            <Text className="text-slate-200 font-medium mt-1">New York, USA</Text>
          </View>
        </View>
      </View>
    </View>
  );
}
