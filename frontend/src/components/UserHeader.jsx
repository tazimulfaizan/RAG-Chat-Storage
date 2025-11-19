import React from 'react';
import { User, LogOut } from 'lucide-react';

function UserHeader({ userId, onLogout }) {
  return (
    <div className="bg-gradient-to-r from-blue-600 to-indigo-600 text-white px-4 py-3 flex items-center justify-between shadow-md">
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 bg-white/20 rounded-full flex items-center justify-center">
          <User size={20} />
        </div>
        <div>
          <p className="text-sm opacity-90">Logged in as</p>
          <p className="font-semibold">{userId}</p>
        </div>
      </div>

      <button
        onClick={onLogout}
        className="flex items-center gap-2 px-4 py-2 bg-white/10 hover:bg-white/20 rounded-lg transition-colors text-sm font-medium"
        title="Switch User"
      >
        <LogOut size={18} />
        Switch User
      </button>
    </div>
  );
}

export default UserHeader;

