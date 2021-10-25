import SwiftUI
import SharedAppLib
import Kingfisher
import SharedAppLib

struct ArticlesView: View {
    
    let state: ArticlesState
    let handler: MessageHandler
    
    @SwiftUI.State var showsAlert: Bool
    @SwiftUI.State private var searchText: String = ""
    
    init(state: ArticlesState, handler: @escaping MessageHandler) {
        self.state = state
        self.handler = handler
        self.showsAlert = state.transientState is ArticlesState.TransientStateException
    }
    
    var body: some View {
        ZStack {
            
            TabView {
                
                VStack(alignment: .leading, spacing: 10) {
                    
                    Text("Feed")
                        .font(.title)
                        .padding()
                    
                    SearchBar(text: $searchText)
                    
                    if (state.isLoading || state.isLoadingNext) && state.articles.isEmpty {
                        ProgressView().scaledToFill()
                    } else {
                        
                        List {
                            
                            //showsAlert = state.transientState is ArticlesState.TransientStateException
                            
                            ForEach(state.articles, id: \.url) { article in
                                RowItem(screenId: state.id, article: article, handler: handler)
                            }
                            
                            VStack(alignment: HorizontalAlignment.center) {
                                if state.isLoadingNext {
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
    let handler: MessageHandler
    let screenId: UUID
    
    // todo how to pass environmental vars implicitly?
    init(screenId: UUID, article: Article, handler: @escaping MessageHandler) {
        dateFormatter.dateFormat = "dd MMM' at 'hh:mm"
        self.article = article
        self.handler = handler
        self.screenId = screenId
    }
    
    var body: some View {
        
        VStack(alignment: .leading, spacing: 10, content: {
            
            if let image = article.urlToImage {
                KFImage.url(image)
                    .resizable()
                    .fade(duration: 0.25)
                    .aspectRatio(contentMode: .fit)
                    .frame(height: 200, alignment: .center)
                // .background(.red) // for layout debug
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
        }).background {
            // Context menu seems keeping old state and doesn't update on state change
            // see: https://stackoverflow.com/questions/68313742/why-does-context-menu-display-the-old-state-even-though-the-list-has-correctly-b
            Color
                .clear
                .contextMenu {
                    Button {
                        showSharePopup()
                        // handler(ShareArticle(article: article))
                    } label: {
                        Label("Share", systemImage: "square.and.arrow.up")
                    }
                    
                    Button {
                        print("article \(article)")
                        handler(ToggleArticleIsFavorite(id: screenId, article: article))
                    } label: {
                        Label(article.isFavorite ? "Remove from favorite" : "Add to favorite", systemImage: article.isFavorite ? "heart.fill" : "heart")
                    }
                }.id(article.isFavorite)
        }
           
        /*.padding(
         EdgeInsets(
         top: CGFloat(16.0),
         leading: CGFloat(16.0),
         bottom: CGFloat(16.0),
         trailing: CGFloat(16.0)
         )
         )*/
    }
    
    private func showSharePopup() {
        // todo rework with AppResolver in mind
        let urlShare = article.url
        let activityVC = UIActivityViewController(activityItems: [urlShare], applicationActivities: nil)
        UIApplication.shared.windows.first?.rootViewController?.present(activityVC, animated: true, completion: nil)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        RowItem(screenId: UUID(), article: Article.init(url: URL(string: "www.google.com")!, title: "Title", author: nil, description: nil, urlToImage: nil, published: Date(), isFavorite: true)) {_ in }
    }
}
