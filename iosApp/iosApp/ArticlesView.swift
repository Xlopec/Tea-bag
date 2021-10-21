import SwiftUI
import SharedAppLib
import Kingfisher
import SharedAppLib


/*let article = Article(
 url: Url(impl: URL(string: "https://www.google.com")!),
 title: Title(value: "Clubhouse is now available for IOS"),
 author: Author(value: "Max Oliynick"),
 description: Description(value: """
 Let your imagination fly! Modifiers let you modify your composable in a very flexible way. For example, if you wanted to add some outer spacing, change the background color of the composable, and round the corners of the Row, you could use the following code
 """),
 urlToImage: Url.init(impl: URL(string: "https://miro.medium.com/max/4000/1*Ir8CdY5D5Do5R_22Vo3uew.png")!),
 published: CommonDate(impl: Date()),
 isFavorite: true
 )
 
 let articles = [article, article, article]*/

class ArticlesViewModel: ObservableObject {
    
    /*enum LoadableLaunches {
     case loading
     case result([RocketLaunch])
     case error(String)
     }*/
    
    @Published var articlesState: ArticlesState
    
    @Published var articles: [Article] = [Article]()
    
    //private let component: IosComponent
    
    init(state: ArticlesState) {
        self.articlesState = state
        //self.component = component
        //load()
    }
    
    func load() {
        
        /* let wrapper = IosComponent(env: EnvironmentKt.PlatformEnv(closeCommandsFlow: { close in
         print("Close app \(close)")
         }))
         
         wrapper.render { state in
         print("New app state \(state)")
         
         let screen = state.screen
         
         switch screen {
         case let articlesState as ArticlesState:
         self.articles = articlesState.articles
         case let settingsState as SettingsState:
         print("Render settings")
         case let articleDetailsState as ArticleDetailsState:
         print("Render article details")
         default:
         fatalError("Unhandled app state: \(state), screen: \(screen)")
         }
         }*/
    }
    
}

struct ArticlesView: View {
    
    let state: ArticlesState
    let handler: MessageHandler
    
    @SwiftUI.State var showsAlert: Bool
    @SwiftUI.State private var searchText: String = ""
    
    init(state: ArticlesState, handler: @escaping MessageHandler) {
        self.state = state
        self.handler = handler
        showsAlert = state.transientState is ArticlesState.TransientStateException
    }
    
    var body: some View {
        ZStack {
            
            TabView {
                
                VStack(alignment: .leading, spacing: 10) {
                    
                    Text("Feed")
                        .font(.title)
                        .padding()
                    
                    SearchBar(text: $searchText)
                    
                    List {
                        
                        //showsAlert = state.transientState is ArticlesState.TransientStateException
                        
                        ForEach(state.articles, id: \.url) { article in
                            RowItem(article: article)
                        }
                        
                        VStack(alignment: HorizontalAlignment.center) {
                            if state.isLoading && state.articles.isEmpty {
                                ProgressView().scaledToFill()
                            } else if state.isLoadingNext {
                                ProgressView()
                            } else if let transientState = state.transientState as? ArticlesState.TransientStateException {
                                Text(transientState.th.message ?? "Failed to load articles, please, try again later")
                            }
                        }
                    }.refreshable {
                        handler(RefreshArticles(id: state.id))
                    }.alert(isPresented: $showsAlert, TextAlert(title: "Title") {_ in
                        print("Loh")
                    })
                    
                }
            
                .tabItem {
                    Image(systemName: "globe")
                    Text("Articles")
                }
                
                Text("pidor")
                .tabItem {
                    Image(systemName: "heart")
                    Text("Favorites")
                }
                
                Text("suka")
                .tabItem {
                    Image(systemName: "chart.line.uptrend.xyaxis")
                    Text("Trending")
                }
                
                Text("blyat")
                .tabItem {
                    Image(systemName: "gear")
                    Text("Settings")
                }
                
            }.font(.headline)
            
            
        }
    }
}

struct RowItem: View {
    
    private let dateFormatter = DateFormatter()
    
    let article: Article
    
    init(article: Article) {
        dateFormatter.dateFormat = "dd MMM' at 'hh:mm"
        self.article = article
    }
    
    var body: some View {
        
        
        VStack(alignment: .leading, spacing: 10, content: {
            
            if let image = article.urlToImage {
                KFImage.url(image)
                    .resizable()
                    .fade(duration: 0.25)
                    .aspectRatio(contentMode: .fit)
                    .frame(height: 200, alignment: .center)
                   // .background(.red)
            }
            // todo get rid of casts
            Text(article.title as! String)
            
            if let author = article.author as? String {
                Text("By \(author)")
                    .font(.caption)
            }
            
            if let description = article.description_ as? String {
                Text(description).font(.subheadline).lineLimit(100)
            }
            
            Text("Published on \(dateFormatter.string(from: article.published))")
                .font(.caption)
            
            HStack(alignment: .center, spacing: 10) {
                
                Spacer()
                
                Button {
                    print("zzzz")
                } label: {
                    Image(systemName: "square.and.arrow.up")
                }
                
                Button {
                    print("trash")
                } label: {
                    Image(systemName: article.isFavorite ? "heart.fill" : "heart")
                }.tint(.red)
            }
            
        })/*.padding(
           EdgeInsets(
           top: CGFloat(16.0),
           leading: CGFloat(16.0),
           bottom: CGFloat(16.0),
           trailing: CGFloat(16.0)
           )
           )*/
    }
}



struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        RowItem(article: Article.init(url: URL(string: "www.google.com")!, title: "Title", author: nil, description: nil, urlToImage: nil, published: Date(), isFavorite: true))
    }
}
