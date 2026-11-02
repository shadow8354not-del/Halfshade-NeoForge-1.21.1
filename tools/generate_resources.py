import pathlib,json,zipfile,hashlib,math,struct,zlib
r=pathlib.Path(__file__).resolve().parents[1]/'src/main/resources'
def put(path,obj):
 p=r/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,ensure_ascii=False,indent=2),encoding='utf8')
def power(n,t,**kw):put('data/poluten/origins/power/'+n+'.json',dict(type='origins:'+t,**kw))
cond={'type':'origins:resource','resource':'poluten:form','comparison':'==','compare_to':1}
for n in ['form','request']:power(n,'resource',min=0,max=1,start_value=0,hidden=True)
power('transform','active_self',name='Превращение в тень',description='Основная способность: проходите сквозь стены. Голод расходуется в 5 раз быстрее. На солнце вы горите. Нельзя использовать предметы и ломать блоки. Выйти из формы внутри блоков нельзя, но при 6 единицах голода она отключается принудительно.',cooldown=1,key={'key':'key.origins.primary_active','continuous':False},entity_action={'type':'origins:change_resource','resource':'poluten:request','change':1,'operation':'set'})
power('phasing','phasing',hidden=True,condition=cond,blacklist=True,block_condition={'type':'origins:in_tag','tag':'origins:unphasable'},render_type='blindness',view_distance=10)
power('appearance','model_color',hidden=True,condition=cond,red=.12,green=.10,blue=.18,alpha=.55)
for name,comparison,color in [('vignette_black','>',(0,0,0)),('vignette_red','<=',(1,.025,.015))]:
 power(name,'empty',hidden=True)
# Procedural UI gradient: fully transparent centre and smoothly shaded edges.
size=256
pixels=bytearray()
for y in range(size):
 pixels.append(0)
 for x in range(size):
  radius=math.sqrt(((x+.5)/size*2-1)**2+((y+.5)/size*2-1)**2)
  t=max(0,min(1,(radius-.25)/.7)); alpha=round(125*t*t*(3-2*t))
  pixels.extend((255,255,255,alpha))
def chunk(t,b):return struct.pack('>I',len(b))+t+b+struct.pack('>I',zlib.crc32(t+b))
png=b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',size,size,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(pixels))+chunk(b'IEND',b'')
texture=r/'assets/poluten/textures/overlay/vignette.png';texture.parent.mkdir(parents=True,exist_ok=True);texture.write_bytes(png)
power('exhaustion','modify_exhaustion',hidden=True,condition=cond,modifier={'operation':'multiply_total_multiplicative','value':4})
power('health','attribute',name='Полумёртвый',description='У вас 7 сердец вместо 10.',modifier={'attribute':'minecraft:generic.max_health','value':-6,'operation':'add_value'})
for n,title,desc in [('night','Ночной ветер','Ночью вы получаете Скорость I.'),('poison','Теневой яд','Удар мечом в теневой форме накладывает Иссушение I на 3 секунды. Общий кулдаун: 6 секунд.'),('last_breath','Последний вздох','Летальный урон оставляет вас в теневой форме с 3,5 сердца, Силой II и Скоростью I. Вас можно добить. Через 10 секунд вы неизбежно умираете. Голод не отключает эту форму.'),('fall','Нет ног','В теневой форме вы не получаете урон от падения.'),('totem','Нет чистой души','Тотемы бессмертия не спасают вас.'),('fire','Тень уничтожает свет','Вы получаете вдвое больше урона от огня и лавы.'),('stomach','Нет желудка','В теневой форме нельзя есть и пить.'),('blindness','Теневая пелена','После выхода из блоков в теневой форме вы получаете слепоту на 2 секунды.')]:power(n,'empty',name=title,description=desc)
powers=[p.stem for p in (r/'data/poluten/origins/power').glob('*.json')]
put('data/poluten/origins/origin/poluten.json',{'name':'Полутень','description':'Великий воин, который пытался победить тьму, проиграл в финальном сражении и стал получеловеком-полутенью.','icon':{'id':'minecraft:echo_shard'},'impact':3,'order':11,'powers':['poluten:'+n for n in powers]})
put('data/origins/tags/origins/origin/origin.json',{'replace':False,'values':['poluten:poluten']})
put('pack.mcmeta',{'pack':{'pack_format':48,'description':'Полутень — Origins NeoForge 0.3 / Minecraft 1.21.1'}})
p=r/'META-INF/neoforge.mods.toml';p.parent.mkdir(parents=True,exist_ok=True);p.write_text('''modLoader="javafml"
loaderVersion="[4,)"
license="MIT"
[[mods]]
modId="poluten"
version="1.0.5"
displayName="Полутень"
description="Происхождение Полутень для Origins NeoForge 0.3."
[[dependencies.poluten]]
modId="neoforge"
type="required"
versionRange="[21.1.228,)"
ordering="NONE"
side="BOTH"
[[dependencies.poluten]]
modId="minecraft"
type="required"
versionRange="[1.21.1]"
ordering="NONE"
side="BOTH"
[[dependencies.poluten]]
modId="origins"
type="required"
versionRange="[0.3]"
ordering="AFTER"
side="BOTH"
''',encoding='utf8')
