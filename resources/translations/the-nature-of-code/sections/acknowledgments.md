# 致谢

> 我们周围的世界以复杂而奇妙的方式运动. 在生命的早期阶段, 我们通过感知与互动来认识环境. 我们期待周围物理世界的行为与感知记忆保持一致. 例如, 石头被松手后会因重力下落; 一阵风吹过时, 较轻的物体会被抛得更远. 本课程关注如何理解、模拟物理世界中的运动元素, 并将它们融入我们创造的数字世界. 我们希望借用用户的感知记忆, 创造更直观、更丰富、更令人满足的体验.
>
> - James Tu, *Dynamic Bodies* 课程说明, 2003 年春季, ITP

2003 年, 我还是 New York University Tisch School of the Arts 的 Interactive Telecommunications Program, ITP, 研究生, 选修了一门名为 *Dynamic Bodies* 的课程. 课程由交互设计师、ITP 兼职教授 James Tu 讲授. 当时, 我的工作集中在一系列实时生成非照片写实图像的软件实验上. 这些应用从实时视频源捕获图像, 再让各种元素按照不同规则在屏幕上移动, 用这些元素 "绘制" 颜色. Tu 的课程涵盖向量、力、振荡、粒子系统、递归、转向和弹簧, 与我的工作完美契合.

我已经在自己的项目中非正式地使用过这些概念, 但从未认真研究算法背后的科学, 也没有学习如何通过面向对象技术将实现形式化. 同一学期, 我还选修了 Philip Galanter 的 *Foundations of Generative Art Systems*. 这门课聚焦生成艺术的理论与实践, 内容包括混沌、细胞自动机、遗传算法、神经网络和分形. Tu 和 Galanter 的课程共同为我打开了仿真算法的世界. 这些技术支撑了我此后数年的创作与教学, 也成为本书的基础和灵感来源.

不过, 这个故事还缺少一块拼图.

Galanter 的课程主要讨论理论, Tu 的课程则使用 Macromedia Director 和 Lingo 编程语言. 那个学期, 我通过把许多算法移植到 C++ 来学习它们. 当时我使用 C++ 还相当笨拙, 而 openFrameworks 和 Cinder 之类的 C++ 创意编程环境尚未出现. 学期临近结束时, 我发现了一个叫 Processing 的东西, 见 [processing.org](https://www.processing.org). 当时 Processing 仍处于 alpha 阶段, 版本号是 0055. 因为我有一点 Java 经验, 便产生了一个问题: 这个开源、对艺术家友好的编程语言与环境, 会不会正适合用来开发一套关于编程与仿真的教程和示例? 在 ITP 和 Processing 社区的支持下, 我开始了一段如今已接近 20 年的编程教学旅程.

首先, 我要感谢 Red Burns. 她领导 ITP 走过了最初 30 年, 并于 2013 年去世. 在十多年时间里, Red 一直支持并鼓励我的工作. Tisch School of the Arts 新兴媒体副院长 Dan O'Sullivan 是我的导师. 最初正是他建议我尝试开设 Processing 课程, 这也给了我整理编程教程的理由. Shawn Van Every 目前担任系主任. 在我全职教学的第一年, 他是我的办公室同事, 多年来一直为我提供大量帮助和灵感. 我也感谢 ITP 教授 Luisa Pereira 的支持和鼓励. 她正在创作的 *Code of Music* 深深启发了我. 她对交互式教育材料的创新方法, 促使我重新思考并重新定义自己的写作和出版流程.

ITP 充满活力且具有滋养性的环境, 由许多杰出的人共同塑造. 无论是本书构思早期的同事, 还是后来带来新鲜灵感的新面孔, 我都由衷感谢 ITP/IMA 的全职教师: Ali Santana、Allison Parrish、Blair Simmons、Clay Shirky、Craig Protzel、Danny Rozin、David Rios、Gabe Barcia-Colombo、Katherine Dillon、Marianne Petit、Marina Zurkow、Matt Romein、Mimi Yin、Nancy Hechinger、Pedro Galvao Cesar de Oliveira、Sarah Rothberg、Sharon De La Cruz、Tom Igoe 和 Yeseul Song.

ITP 以及 NYU Interactive Media Arts, IMA, 那些敬业而不知疲倦的工作人员, 对维持整个生态系统的运转、让一切真正发生, 起着至关重要的作用. 感谢多年来与我合作的 Adrian Mandeville、Brian Kim、Daniel Tsadok、Dante Delgiacco、Edward Gordon、Emma Asumeng、George Agudow、John Duane、Lenin Compres、Luke Bunn、Marlon Evans、Matt Berger、Megan Demarest、Midori Yasuda、Phil Caridi、Rob Ryan、Scott Broussard 和 Shirley Lin.

还要特别感谢 ITP 兼职教师 Ellen Nickles 和 Nuntinee Tansrisakul. 在全球疫情最严重的 2021 年, 她们与我共同讲授了 Nature of Code 课程的在线异步版本. 她们的贡献以及那个学期产生的想法, 极大丰富了课程材料.

ITP 和 IMA 的学生人数太多, 无法在这里逐一列出. 在整个过程中, 他们一直是极其宝贵的反馈来源. 本书许多材料来自我的同名课程, 这门课我已经教授了 17 次. 我保存着一摞摞写满页边批注的书稿打印件, 还有一个庞大的学生邮件档案, 其中包含纠错、评论和慷慨的鼓励.

我想特别提到几位作为研究生助理参与 Nature of Code 材料工作的学生. Briana Jones 和 Chaski No 通过 ITP/IMA Equitable Syllabus 项目提供了非凡的研究支持, 扩展了本书的概念与参考资料. Gracy Whelihan 是第一期本科版 Nature of Code 课程的研究生助教, 她提供了宝贵支持和反馈, 并总能提醒我随机数的神奇之处.

Jason Gao 和 Stuti Mohgaonkar 参与了本书材料的构建系统, 发明了新的写作与编辑工作流. Elias Jarzombek 也值得特别感谢. 他从 *Code of Music* 项目中积累的经验, 为本书提供了建议和技术支持.

毕业后, Jason Gao 继续设计并开发本书的构建系统和网站, 见 [natureofcode.com](https://natureofcode.com). 如果你现在访问那里, 会看到他多种才能结出的成果: 完整版图书与 p5.js Web Editor 无缝集成. 最终实现远远超出了我最初的设想.

本书内页与网站由 Tuan Huang 精心设计. 2023 年春季, Tuan 在选修 Nature of Code 课程期间开始构思版式. 毕业后, 她继续打磨设计, 在本书众多元素之间建立统一的视觉语言. 她极简而优雅的美学显著提升了本书的视觉吸引力和可访问性. 还要特别感谢 OpenMoji 项目, 见 [openmoji.org](https://openmoji.org). 这是一个采用 CC BY-SA 4.0 许可的开源 emoji 和图标项目, 为本书不同元素提供了令人愉悦且内容丰富的图标集合.

我也深深受惠于充满活力、彼此支持的创意编程社区和 Processing Foundation. 如果没有 2001 年创建 Processing 并共同创立 Processing Foundation 的 Casey Reas 和 Ben Fry, 我不会写出这本书. 他们投入了 20 多年时间建设和维护软件及其社区. 我所知的一半知识, 都来自阅读 Processing 源代码和文档. Processing 语言、网站与 IDE 的优雅简洁, 是我全部工作和教学最初的灵感来源.

p5.js 的创造者 Lauren Lee McCarthy 播下了那颗让本书转向 JavaScript 成为可能的种子. 她始终不知疲倦地推动开源领域的包容性与可访问性, 她建设社区的方式对我影响深远. Cassie Tarakajian 发明了 p5.js Web Editor. 这是一次英雄般的工程, 让本书所有示例代码得以被收集和组织.

我衷心感谢 Processing Foundation 现任和前任董事会成员, 除 Casey、Ben 和 Lauren 外, 还包括 Dorothy Santos、Johanna Hedva、Kate Hollenbach 和 Xin Xin. 还要特别感谢基金会的项目负责人、员工和校友. 每个人都在塑造和推动社区及其项目的过程中发挥了关键作用: Andres Colubri、Charles Reinhardt、evelyn masso、Jesse C. Thompson、Jonathan Feinberg、Moira Turner、Qianqian Ye、Rachel Lim、Raphaël de Courville、Saber Khan、Suhyun (Sonia) Choi、Toni Pizza、Tsige Tafesse 和 Xiaowei R. Wang.

在第 10 章中, 我将介绍 ml5.js 项目. 它是 p5.js 的配套库, 目标是以友好、易接近的方式把机器学习能力带给创意程序员. 感谢 ITP/IMA 中参与开发的众多研究人员与学生: Apoorva Avadhana、Ashley Lewis、Bomani McClendon、Christina Dacanay、Cristóbal Valenzuela、Lydia Jessup、Miaoye Que、Micaelle Lages、Michael Weinberg、Orpheas Kofinakos、Ozi Chukwukeme、Sam Krystal、Yining Shi 和 Ziyuan (Peter) Lin. 感谢 NYU Shanghai 的 J.H. Moon 教授、Gottfried Haider 教授和 Quinn Fangqing He. 他们也支持了该库的开发, 并慷慨阅读了神经网络章节的早期草稿. Linda Paiste 通过志愿工作帮助改进代码库, 同样值得感谢. 最后, 我要特别感谢 Joey K. Lee. 他在开发 ml5.js 的同时, 为 Nature of Code 本身提供了宝贵的鼓励和反馈.

我还要感谢 AI 研究者 David Ha. 他关于神经进化的研究, 参见本书网站的 "Additional Resources", 激励我用 ml5.js 实现相关技术示例, 并为本书新增一个章节.

过去 10 年中, 我大部分时间都在为 YouTube 频道 The Coding Train 制作视频教程. 我非常感谢许多人给予的大力支持与合作, 让列车的引擎持续运转并尽量保持在轨道上, 即使我总是很努力地想要偏离轨道. 他们包括 Chloe Desaulles、Cy X、David Snyder、Dusk Virkus、Elizabeth Perez、Jason Heglund、Katie Chan、Kline Gareth、Kobe Liesenborgs 和 Mathieu Blanchette. 还要特别感谢 Melissa Rodriguez. 她帮助研究并取得了每章开头图片的使用许可.

也感谢 Nebula 流媒体平台及其 CEO Dave Wiskus 的坚定支持, 以及 Nebula 创作者 Grady Hillhouse. 正是他建议我与 No Starch Press 合作, 真正把这玩意儿印出来. 如果没有 YouTube 平台本身, 我也无法触达如此广泛的受众. 特别感谢我卓越的 YouTube 合作伙伴经理 Dean Kowalski, 以及帮助领导 YouTube North America Skilling 的 Doreen Tran.

我拥有许多体贴、聪明、慷慨而友善的观众. 特别感谢 Dipam Sen、Francis Turmel、Kathy McGuiness 和 Simon Tiger. 他们提供了建议、反馈、纠错、技术支持以及更多帮助. 因为他们, 本书变得更好.

我还要感谢十多年前参与 2012 版工作的许多人: David Wilson, 负责封面和设计; Rune Madsen 与 Steve Klise, 负责构建系统和网站; Shannon Fry, 负责编辑; Evan Emolo、Miguel Bermudez, 以及所有帮助资助该项目的 Kickstarter 支持者.

还要特别提到 Zannah Marsh. 她曾不知疲倦地为 2012 版创作 100 多幅插画, 又以近乎奇迹的方式同意为新版全部重做. 我尤其感谢她的耐心, 以及当我对某些插图反复改变主意时, 她仍愿意顺势调整. 还有那些猫. 每次看到它们敲键盘, 我都会笑得合不拢嘴.

现在说到我们真正聚在这里的原因. 如果没有 No Starch Press, 我几乎可以肯定你不会读到这些文字. 当然, 你也许会在网站上浏览更新后的教程, 但真正推动我跨过最后一道坎的, 是团队的合作、支持, 以及体贴而友善的截止日期安排.

我要感谢编辑 Nathan Heidelberger. 本书之所以能够讲得通, 基本都归功于他, 更不用说那些确实很好笑的笑话了. 至于糟糕的双关语, 责任完全在我. 感谢技术编辑 Jasper Palfree. 他耐心地向我解释线性运动与转动之间的差异, 解释了多少遍才让我真正理解就解释多少遍, 同时还澄清了无数其他科学与代码概念. 特别感谢文字编辑 Sharon Wilkey. 她对细节的细致关注打磨了每个句子, 完成了恰到好处的最后修饰. 还要感谢 Audrey Doyle 敏锐的校对眼光.

感谢 No Starch 创始人 Bill Pollock. 他教会了我在 Trader Joe's 购物所需的一切. 感谢执行编辑 Jill Franklin 的友善和耐心支持. 感谢由高级制作编辑 Jennifer Kepler 与制作经理 Sabrina Plomitallo-González 领导的制作团队, 他们包容了我不同寻常的 Notion -&gt; GitHub -&gt; PDF 工作流.

最后, 衷心感谢我的妻子 Aliki Caloyeras. 她总是对的. 真的, 这如今已经像一种超能力. 我爱你. 也感谢我的孩子们. Elias 慷慨地允许我保留一丝尊严, 没有在篮球和电子游戏中把我彻底碾碎. Olympia 则在我们玩双陆棋和纸牌并一起大笑时, 提醒我 "I'm feeling 22". 我还要感谢父亲 Bernard Shiffman. 他慷慨提供数学专业知识并一路给予反馈. 同样感谢母亲 Doris Yaffe Shiffman 和兄弟 Jonathan Shiffman. 他们始终用一个问题给予巨大支持: "书写得怎么样了?"
