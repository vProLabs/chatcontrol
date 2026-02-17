New Features:
- LuckPerms Integration (Optional) - Full support for LuckPerms prefixes and suffixes in chat, group-based chat bypass permissions (give entire groups ability to chat when chat is disabled via /lp group &lt;group&gt; permission set vchatutils.bypass true), automatic detection and hooking on server startup
- Command Alias Toggles - New config options: polish-aliases and english-aliases, enable/disable Polish commands (/chat wyczysc, /chat wlacz, etc.) independently, enable/disable English commands (/chat clear, /chat on, etc.) independently
- Leave Message Control - Added hide-leave-messages option (separate from join messages), control join and leave messages independently

Changes:
- Default values changed: hide-join-messages: true -&gt; false, hide-leave-messages: false (new), hide-advancements: true -&gt; false, chat remains enabled by default (chat-enabled: true)
- Enhanced Status Command - Now shows LuckPerms integration status, shows which groups have vchatutils.bypass permission (admin only)

Technical:
- Added softdepend: [LuckPerms] in plugin.yml
- Improved permission checking with group inheritance support
- Better error handling for LuckPerms hook failures
